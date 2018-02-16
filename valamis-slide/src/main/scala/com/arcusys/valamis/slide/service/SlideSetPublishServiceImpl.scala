package com.arcusys.valamis.slide.service

import java.io.InputStream
import java.util.UUID
import javax.servlet.ServletContext

import com.arcusys.learn.liferay.services.{CompanyHelper, GroupLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lesson.model.{Lesson, LessonType, PackageActivityType}
import com.arcusys.valamis.lesson.service.{LessonAssetHelper, LessonNotificationService, LessonService}
import com.arcusys.valamis.lesson.tincan.model.{LessonCategoryGoal, TincanActivity}
import com.arcusys.valamis.lesson.tincan.service.{LessonCategoryGoalService, TincanPackageService}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrs.tincan.{Activity, AuthorizationScope, Statement, Verb}
import com.arcusys.valamis.lrssupport.lrs.service.{LrsClientManager, LrsRegistration}
import com.arcusys.valamis.slide.model.{SlideSet, SlideSetStatus}
import com.arcusys.valamis.slide.service.export.TincanLessonBuilder
import com.arcusys.valamis.slide.storage.SlideSetRepository
import com.arcusys.valamis.statements.StatementChecker
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.util.StreamUtil
import com.arcusys.valamis.utils.TincanHelper
import org.joda.time.DateTime
import com.arcusys.valamis.utils.TincanHelper.TincanAgent
import com.arcusys.valamis.utils.TincanHelper




abstract class SlideSetPublishServiceImpl extends SlideSetPublishService {
  def slideService: SlideService
  def slideSetService: SlideSetService
  def lessonService: LessonService
  def tincanPackageService: TincanPackageService
  def slideSetRepository: SlideSetRepository
  def lessonBuilder: TincanLessonBuilder
  def packageCategoryGoalStorage: LessonCategoryGoalService
  def lessonAssetHelper: LessonAssetHelper
  def lessonSocialActivityHelper: SocialActivityHelper[Lesson]
  def uriService: TincanURIService
  def slideTagService: TagService[SlideSet]
  def lessonTagService: TagService[Lesson]
  def lessonNotificationService: LessonNotificationService
  def lrsReader : LrsClientManager
  def lrsRegistration : LrsRegistration
  def statementChecker: StatementChecker

  //TODO: remove method, slide set should not require temp lesson
  override def findPublishedLesson(slideSetId: Long, userId: Long): Lesson = {
    val slideSet = slideSetService.getById(slideSetId)

    tincanPackageService.getActivity(slideSet.activityId) match {
      case Some(activity) => lessonService.getLessonRequired(activity.lessonId)
      case None =>
        val lesson = lessonService.create(
          LessonType.Tincan,
          slideSet.courseId,
          slideSet.title,
          slideSet.description,
          userId,
          slideSet.scoreLimit,
          slideSet.requiredReview
        )
        createTincanActivity(slideSet, lesson.id, "index.html")

        lesson
    }
  }

  override def publish(servletContext: ServletContext, id: Long, userId: Long, courseId: Long): Unit = {
    val slideSet = slideSetService.getById(id)

    slideSetRepository.getByActivityId(slideSet.activityId)
      .filter(_.status == SlideSetStatus.Published)
      .filterNot(_.id == id)
      .foreach(s => updateStatus(s.id, SlideSetStatus.Archived))

    updateStatus(id, SlideSetStatus.Published, new DateTime)

    val packageFiles = lessonBuilder.composeTinCanPackage(
      servletContext,
      id)

    val oldLesson = findPublishedLesson(id, userId)
    packageCategoryGoalStorage.delete(oldLesson.id)

    val lesson = uploadSlideSet(
      slideSet,
      packageFiles,
      courseId,
      userId,
      Some(oldLesson.id)
    )

    val packageAssetId = lessonAssetHelper.updatePackageAssetEntry(lesson)

    lessonSocialActivityHelper.addWithSet(
      GroupLocalServiceHelper.getGroup(lesson.courseId).getCompanyId,
      lesson.ownerId,
      courseId = Some(lesson.courseId),
      `type` = Some(PackageActivityType.Published.id),
      classPK = Some(lesson.id),
      createDate = DateTime.now)

    for (logo <- slideSetService.getLogo(id)) {
      lessonService.setLogo(lesson.id, slideSet.logo.get, logo)
    }

    val packageGoals = slideService.getSlides(id)
      .flatMap(_.statementCategoryId)
      .flatMap(uriService.getById(_, TincanURIType.Category))
      .groupBy(identity).map { case (key, items) => (key, items.size) }
      .map { case (u, size) =>
        LessonCategoryGoal(
          lessonId = lesson.id,
          name = u.content,
          category = u.uri,
          count = size
        )
      }
      .toSeq

    packageCategoryGoalStorage.add(packageGoals)

    val tagsIds = slideTagService.getByItemId(slideSet.id).map(_.id)
    lessonTagService.setTags(packageAssetId, tagsIds)
    if (slideSet.status == SlideSetStatus.Draft && slideSet.version == 1.0) {
      lessonNotificationService.sendLessonAvailableNotification(Seq(lesson), lesson.courseId)

      sendStatement(userId, slideSet)
    }
  }

  private def createTincanActivity(slideSet: SlideSet, lessonId: Long, launch: String): Unit = {
    val activity = TincanActivity(
      lessonId,
      slideSet.activityId,
      "http://adlnet.gov/expapi/activities/course",
      slideSet.title,
      slideSet.description,
      Some(launch),
      None)

    tincanPackageService.addActivity(activity)
  }

  private def updateStatus(id: Long, status: String, date: DateTime): Unit = {
    slideSetRepository.updateStatusWithDate(id, status, date)
  }

  private def updateStatus(id: Long, status: String): Unit = {
    slideSetRepository.updateStatus(id, status)
  }

  private def uploadSlideSet(slideSet: SlideSet,
                             files: Map[String, InputStream],
                             courseId: Long,
                             userId: Long,
                             existLessonId: Option[Long]): Lesson = {

    val lesson = existLessonId match {
      case None =>
        lessonService.create(
          LessonType.Tincan,
          courseId,
          slideSet.title,
          slideSet.description,
          userId,
          slideSet.scoreLimit,
          slideSet.requiredReview)

      case Some(id) =>
        val oldLesson = lessonService.getLessonRequired(id)

        lessonService.update(oldLesson.copy(
          title = slideSet.title,
          description = slideSet.description,
          ownerId = userId,
          creationDate = DateTime.now,
          scoreLimit = slideSet.scoreLimit.getOrElse(0.7),
          requiredReview = slideSet.requiredReview
        ))

        tincanPackageService.deleteResources(id)

        lessonService.getLessonRequired(id)
    }

    val manifestStream = files("tincan.xml")
    manifestStream.mark(0)
    tincanPackageService.updateActivities(lesson.id, manifestStream)
    manifestStream.reset()

    files.foreach { case (filename, stream) =>
      tincanPackageService.addFile(lesson.id, filename, StreamUtil.toByteArray(stream))
    }

    lesson
  }

  private def sendStatement(userId: Long,
                            slideSet: SlideSet): Unit = {
    lazy val companyId = CompanyHelper.getCompanyId
    val verb = Verb("http://adlnet.gov/expapi/verbs/imported", Map("en-US" -> "imported"))
    val statement = Statement(
      Option(UUID.randomUUID),
      UserLocalServiceHelper().getUser(userId).getAgentByUuid,
      verb,
      Activity(
        id = slideSet.activityId,
        name = Some(Map("en-US" -> slideSet.title)),
        theType = Some("http://adlnet.gov/expapi/activities/course"),
        description = Some(Map("en-US" -> slideSet.description))),
      timestamp = DateTime.now,
      stored = DateTime.now
    )
    val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All,
      host = PortalUtilHelper.getLocalHostUrl)(companyId).auth

    statementChecker.checkStatements(Seq(statement))
    lrsReader.statementApi(_.addStatement(statement), Some(lrsAuth))(CompanyHelper.getCompanyId)
  }
}
