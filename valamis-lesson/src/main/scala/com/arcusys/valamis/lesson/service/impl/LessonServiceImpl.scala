package com.arcusys.valamis.lesson.service.impl

import com.arcusys.learn.liferay.constants.StringPoolHelper
import com.arcusys.learn.liferay.services.{AssetEntryLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.exception.NoLessonException
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.service.{CustomLessonService, LessonAssetHelper, LessonNotificationService, LessonService}
import com.arcusys.valamis.lesson.storage.query._
import com.arcusys.valamis.lesson.storage.{LessonAttemptsTableComponent, LessonTableComponent}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.user.model.User
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by mminin on 21.01.16.
  */
abstract class LessonServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends LessonService
    with LessonTableComponent
    with LessonQueries
    with LessonPlayerQueries
    with LessonLimitQueries
    with LessonViewerQueries
    with LessonAttemptsQueries
    with LessonAttemptsTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  def ratingService: RatingService[Lesson]

  def tagService: TagService[Lesson]

  def assetHelper: LessonAssetHelper

  def socialActivityHelper: SocialActivityHelper[Lesson]

  def userService: UserLocalServiceHelper

  def fileService: FileService

  def fileStorage: FileStorage

  def customLessonServices: Map[LessonType, CustomLessonService]

  def lessonNotificationService: LessonNotificationService

  private def logoPathPrefix(packageId: Long) = s"package_logo_$packageId/"

  private def getFullLogoPath(packageId: Long, logo: String) = "files/" + logoPathPrefix(packageId) + logo


  override def create(lessonType: LessonType,
                      courseId: Long,
                      title: String,
                      description: String,
                      ownerId: Long,
                      scoreLimit: Option[Double] = None,
                      requiredReview: Boolean = false): Lesson = {
    val logo = None
    val isVisible = Some(true)

    val lessonId = execSync {
      lessons
        .map(l => (
          l.lessonType,
          l.title,
          l.description,
          l.logo,
          l.courseId,
          l.isVisible,
          l.ownerId,
          l.creationDate,
          l.scoreLimit,
          l.requiredReview))
        .returning(lessons.selectId) += (
        lessonType,
        title,
        description,
        logo,
        courseId,
        isVisible,
        ownerId,
        DateTime.now,
        scoreLimit.getOrElse(0.7),
        requiredReview
      )
    }

    getLessonRequired(lessonId)
  }

  override def getLesson(id: Long): Option[Lesson] = execSync {
    lessons.filterById(id).result.headOption
  }

  override def getLessonTitlesByIds(ids: Seq[Long]): Seq[(Long, String)] = execSync {
    lessons.filterByIds(ids) map { lesson =>
      (lesson.id, lesson.title)
    } result
  }

  def getLessonForPublicApi(id: Long): Option[(Lesson, Option[LessonLimit])] = execSync {
    (lessons.filterById(id) joinLeft lessonLimits on { (lessons, limits) =>
      lessons.id === limits.lessonId
    }).result.headOption
  }

  override def getRootActivityId(id: Long): String = {
    execSync {
      lessons.filterById(id).selectType.result.headOption
    } match {
      case Some(lessonType) => customLessonServices(lessonType).getRootActivityId(id)
      case None => throw new NoLessonException(id)
    }
  }

  def getRootActivityId(lesson: Lesson): String = {
    customLessonServices(lesson.lessonType).getRootActivityId(lesson.id)
  }

  override def getByRootActivityId(activityId: String): Seq[Long] = {
    customLessonServices.toStream
      .flatMap { case (lessonType, service) => service.getLessonIdByRootActivityId(activityId) }
  }

  override def getLessonRequired(id: Long): Lesson = {
    getLesson(id) getOrElse (throw new EntityNotFoundException(s"Lesson not found, id $id"))
  }

  override def getCount(courseId: Long, titleFilter: Option[String]): Int = execSync {
    //visible ?
    lessons
      .filterByCourseId(courseId)
      .filterByTitle(titleFilter)
      .length.result
  }

  override def getCountByCourses(courseIds: Seq[Long]): Int = execSync {
    //visible ?
    lessons.filterByCourseIds(courseIds).length.result
  }

  def getLogo(id: Long): Option[Array[Byte]] = {
    val lesson = getLesson(id)

    val logoPath = lesson.flatMap(_.logo).map(getFullLogoPath(id, _))

    logoPath.flatMap(fileService.getFileContentOption)
  }

  def setLogo(id: Long, name: String, content: Array[Byte]): Unit = {
    val lesson = getLessonRequired(id)

    fileService.setFileContent(
      folder = logoPathPrefix(lesson.id),
      name = name,
      content = content,
      deleteFolder = true
    )

    updateLogo(id, Some(name))
  }

  override def delete(id: Long): Unit = {
    getLesson(id) match {
      case None =>
      case Some(lesson) =>

        val customService = customLessonServices(lesson.lessonType)

        if (lesson.logo.isDefined) {
          fileService.deleteByPrefix(logoPathPrefix(lesson.id))
        }

        ratingService.deleteRatings(id)

        customService.deleteResources(id)

        execSyncInTransaction {
          DBIO.seq(
            playerLessons.filterBy(id).delete,
            lessonLimits.filterByLessonId(id).delete,
            lessonViewers.filterByLessonId(id).delete,
            lessonAttempts.filterByLessonId(id).delete
          ) andThen {
            lessons.filterById(id).delete
          }
        }

        assetHelper.deleteAssetEntry(id, lesson)
        socialActivityHelper.deleteActivities(id)
    }
  }

  override def deleteLogo(id: Long): Unit = {
    fileService.deleteByPrefix(logoPathPrefix(id))
    updateLogo(id, None)
  }

  override def update(id: Long,
                      title: String,
                      description: String,
                      isVisible: Option[Boolean],
                      beginDate: Option[DateTime],
                      endDate: Option[DateTime],
                      requiredReview: Boolean,
                      scoreLimit: Double): Lesson = execSync {
    updateQ(id)
      .update((
        title,
        description,
        isVisible,
        beginDate,
        endDate,
        requiredReview,
        scoreLimit)
      )
      .andThen {
        filterByIdQ(id).result.head
      }
  }

  override def update(id: Long,
                      title: String,
                      description: String,
                      isVisible: Option[Boolean],
                      beginDate: Option[DateTime],
                      endDate: Option[DateTime],
                      tagIds: Seq[Long],
                      requiredReview: Boolean,
                      scoreLimit: Double): Unit = {
    val lesson =
      update(id, title, description, isVisible, beginDate, endDate, requiredReview, scoreLimit)

    val assetId = assetHelper.updatePackageAssetEntry(lesson)
    tagService.setTags(assetId, tagIds)
  }

  override def updateLessonTags(id: Long, tagIds: Seq[Long]): Unit = {
    val asset = assetHelper.getEntry(id).getOrElse {
      throw new EntityNotFoundException("There is no asset entry for lesson with id: " + id)
    }
    tagService.setTags(asset.getPrimaryKey, tagIds)
  }

  override def update(lesson: Lesson): Unit = {
    execSync {
      lessons
        .filterById(lesson.id)
        .map(l => (
          l.title,
          l.description,
          l.isVisible,
          l.beginDate,
          l.endDate,
          l.ownerId,
          l.creationDate,
          l.scoreLimit,
          l.requiredReview))
        .update((lesson.title, lesson.description, lesson.isVisible, lesson.beginDate,
          lesson.endDate, lesson.ownerId, lesson.creationDate, lesson.scoreLimit, lesson.requiredReview))
    }

    assetHelper.updatePackageAssetEntry(lesson)
  }

  override def updateLessonsInfo(lessonsInfo: Seq[LessonInfo]): Unit = {

    val newLessons: Seq[Lesson] = lessonsInfo.map { info =>
      val lesson = execSync {
        lessons.filterById(info.id)
          .map(l => (l.title, l.description))
          .update((info.title, info.description))
          .andThen {
            lessons.filterById(info.id).result.head
          }
      }

      assetHelper.updatePackageAssetEntry(lesson)
      lesson
    }
    if (!newLessons.isEmpty)
      lessonNotificationService.sendLessonAvailableNotification(newLessons, newLessons.head.courseId)
  }

  override def updateVisibility(lessonId: Long, isVisible: Option[Boolean]): Unit = {
    val lesson = execSync {
      lessons.filterById(lessonId)
        .map(_.isVisible)
        .update(isVisible)
        .andThen {
          lessons.filterById(lessonId).result.head
        }
    }

    assetHelper.updatePackageAssetEntry(lesson)
  }

  override def getAll(courseId: Long): Seq[Lesson] = execSync {
    lessons.filterByCourseId(courseId).result
  }

  override def getLessonsForPublicApi(courseId: Long, lessonType: Option[LessonType],
                                      skipTake: Option[SkipTake]): Seq[(Lesson, Option[LessonLimit])] = execSync {
    val lessonsQ = lessons
      .filterByCourseId(courseId)
      .filterByType(lessonType)
      .joinLeft(lessonLimits).on((lessons, limits) => lessons.id === limits.lessonId)
      .sortBy { case (l, _) => l.title }

    (skipTake match {
      case Some(SkipTake(skip, take)) => lessonsQ.drop(skip).take(take)
      case None => lessonsQ
    }).result
  }

  def getByCourses(courseIds: Seq[Long]): Seq[Lesson] = execSync {
    lessons.filterByCourseIds(courseIds).result
  }

  override def getAllSorted(courseId: Long,
                            titleFilter: Option[String],
                            ascending: Boolean,
                            skipTake: Option[SkipTake]): Seq[Lesson] = execSync {
    lessons
      .filterByCourseId(courseId)
      .filterByTitle(titleFilter)
      .sortByTitle(ascending)
      .slice(skipTake)
      .result
  }

  override def getSortedByCourses(courseIds: Seq[Long],
                                  titleFilter: Option[String],
                                  ascending: Boolean,
                                  skipTake: Option[SkipTake]): Seq[Lesson] = execSync {
    lessons
      .filterByCourseIds(courseIds)
      .filterByTitle(titleFilter)
      .sortByTitle(ascending)
      .slice(skipTake)
      .result
  }

  def getInReview(courseId: Long): Seq[Lesson] = execSync {
    lessons
      .filterByCourseId(courseId)
      .filterByInReview
      .result
  }

  def getInReviewByCourses(courseIds: Seq[Long]): Seq[Lesson] = execSync {
    lessons
      .filterByCourseIds(courseIds)
      .filterByInReview
      .result
  }

  def getWithLimit(lessonId: Long): (Lesson, Option[LessonLimit]) = execSync {
    for {
      lesson <- lessons.filterById(lessonId).result.head
      limit <- lessonLimits.filterByLessonId(lessonId).result.headOption
    } yield {
      (lesson, limit)
    }
  }

  def getAllWithLimits(courseId: Long): Seq[(Lesson, Option[LessonLimit])] = {
    val lessonsQ = lessons.filterByCourseId(courseId)
    val limitsQ = lessonsQ
      .join(lessonLimits).on((l, lim) => l.id === lim.lessonId)
      .map { case (l, lim) => lim }

    val (courseLessons, limits) = execSync {
      for {
        courseLessons <- lessonsQ.result
        limits <- limitsQ.result
      } yield {
        (courseLessons, limits)
      }
    }

    courseLessons.map(l => (l, limits.find(lim => lim.lessonId == l.id)))
  }

  /**
    * get all visible lessons
    * @param courseId scope
    * @param extraVisible add lessons with external visible configuration to result
    *                     (isVisible undefined in lesson table)
    */
  def getAllVisible(courseId: Long, extraVisible: Boolean): Seq[Lesson] = execSync {

    val visibleCondition: (LessonTable => Rep[Option[Boolean]]) = if (extraVisible)
      l => l.isVisible === true || l.isVisible.isEmpty
    else
      l => l.isVisible === true

    lessons
      .filterByCourseId(courseId)
      .filter(visibleCondition)
      .result
  }

  override def getLessonsWithData(criterion: LessonFilter,
                                  ascending: Boolean,
                                  skipTake: Option[SkipTake]
                     ): RangeResult[LessonFull] = {

    val lessonIdsByTag = criterion.tagId.map(tagService.getItemIds)

    if (lessonIdsByTag.exists(_.isEmpty)) {
      RangeResult(0, Nil)
    } else {
      var query = lessons
        .filterByCourseIds(criterion.courseIds)
        .filterByType(criterion.lessonType)
        .filterByTitle(criterion.title)
        .filterVisible(criterion.onlyVisible)

      for (ids <- lessonIdsByTag) {
        query = query.filterByIds(ids)
      }

      //we can't use query.length
      //select count() ... where ID in (...) - falls on hypersonic (liferay version)
      val (countItems, resultLessons) = execSync {
        for {
          countItems <- query.map(_.id).result
          resultLessons <- query.sortByTitle(ascending).slice(skipTake).result
        } yield {
          (countItems, resultLessons)
        }
      }

      val result = RangeResult(countItems.size, resultLessons)

      if (result.records.isEmpty) {
        RangeResult(result.total, Nil)
      } else {
        fillLessonInfo(result)
      }
    }
  }

  private def fillLessonInfo(result: RangeResult[Lesson]): RangeResult[LessonFull] = {
    val ids = result.records.map(_.id)

    val tags = ids.map(id => (id, tagService.getByItemId(id))).toMap
    val limits = execSync {
      lessonLimits.filterByLessonIds(ids).result
    } map {
      limit => (limit.lessonId, limit)
    } toMap

    val userIds = result.records.map(_.ownerId).distinct
    val users = userService.getUsers(userIds).map(u => new User(u))

    result.map { lesson => LessonFull(
      lesson,
      limits.get(lesson.id),
      tags.getOrElse(lesson.id, Nil),
      users.find(_.id == lesson.ownerId)
    )
    }
  }

  override def getTagsFromCourse(courseId: Long): Seq[ValamisTag] = {
    val lessonIds = execSync {
      lessons.filterByCourseId(courseId).selectId.result
    }

    tagService.getByItemIds(lessonIds)
  }

  override def getTagsFromCourses(courseIds: Seq[Long]): Seq[ValamisTag] = {
    val lessonIds = execSync {
      lessons.filterByCourseIds(courseIds).selectId.result
    }

    tagService.getByItemIds(lessonIds)
  }

  override def isExisted(lessonId: Long): Boolean = execSync {
    lessons.filterById(lessonId).map(_.id).take(1).result.headOption.map(_.isDefined)
  }

  private def updateLogo(id: Long, name: Option[String]) = execSync {
    lessons.filterById(id)
      .map(_.logo)
      .update(name)
  }


  override def getLessonURL(lesson: Lesson, companyId: Long, plId: Option[Long] = None): String = {

    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(classOf[Lesson].getName, lesson.id)

    val sb = new StringBuilder()
    sb.append(PortalUtilHelper.getLocalHostUrlForCompany(companyId))
    sb.append(PortalUtilHelper.getPathMain)
    sb.append("/portal/learn-portlet/open_package")
    sb.append(StringPoolHelper.QUESTION)
    sb.append("plid")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(plId.getOrElse("")))
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("resourcePrimKey")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(assetEntry.getEntryId))

    sb.toString
  }
}
