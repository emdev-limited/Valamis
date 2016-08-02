package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.{UserLocalServiceHelper, SocialActivityLocalServiceHelper}
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.service.{TeacherLessonGradeService, LessonService, UserLessonResultService}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.model.PeriodTypes
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

import scala.util._

class CertificateServiceImpl(implicit val bindingModule: BindingModule)
  extends Injectable
  with CertificateService
  with CertificateGoalServiceImpl
  with CertificateUserServiceImpl {

  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateStatusRepository = inject[CertificateStateRepository]
  private lazy val courseGoalRepository = inject[CourseGoalStorage]
  private lazy val activityGoalRepository = inject[ActivityGoalStorage]
  private lazy val statementGoalRepository = inject[StatementGoalStorage]
  private lazy val packageGoalRepository = inject[PackageGoalStorage]
  private lazy val assignmentGoalRepository = inject[AssignmentGoalStorage]
  private lazy val fileService = inject[FileService]
  private lazy val assetHelper = new CertificateAssetHelper()
  private lazy val className = classOf[Certificate].getName
  private lazy val stateSocialActivity = new SocialActivityHelper(CertificateStateType)
  private lazy val goalStateRepository = inject[CertificateGoalStateRepository]
  private lazy val certificateMemberService = inject[CertificateMemberService]
  private lazy val goalRepository = inject[CertificateGoalRepository]
  private lazy val goalGroupRepository = inject[CertificateGoalGroupRepository]
  private lazy val lessonService = inject[LessonService]
  private lazy val teacherGradeService = inject[TeacherLessonGradeService]
  private lazy val gradeService = inject[LessonGradeService]
  private lazy val assignmentService = inject[AssignmentService]

  private def logoPathPrefix(certificateId: Long) = s"$certificateId/"

  private def logoPath(certificateId: Long, logo: String) = "files/" + logoPathPrefix(certificateId) + logo

  def create(companyId: Long, title: String, description: String): Certificate = {
    certificateRepository.create(new Certificate(
      0,
      title,
      description,
      companyId = companyId,
      createdAt = new DateTime)
    )
  }

  override def getLogo(id: Long) = {
    def getLogo(certificate: Certificate) = {
      val logoOpt = if (certificate.logo == "") None else Some(certificate.logo)

      logoOpt
        .map(logoPath(id, _))
        .flatMap(fileService.getFileContentOption)
        .get
    }

    certificateRepository.getByIdOpt(id)
      .map(getLogo)
  }

  override def setLogo(certificateId: Long, name: String, content: Array[Byte]) = {
    val certificate = certificateRepository.getById(certificateId)
    fileService.setFileContent(
      folder = logoPathPrefix(certificateId),
      name = name,
      content = content,
      deleteFolder = true
    )

    certificateRepository.update(certificate.copy(logo = name))
  }

  def update(id: Long,
             title: String,
             description: String,
             periodType: PeriodTypes.PeriodType,
             periodValue: Int,
             isOpenBadgesIntegration: Boolean,
             shortDescription: String = "",
             companyId: Long,
             userId: Long,
             scope: Option[Long],
             optionGoals: Int): Certificate = {

    val stored = certificateRepository.getById(id)

    val (period, value) = if (periodValue < 1)
      (PeriodTypes.UNLIMITED, 0)
    else
      (periodType, periodValue)

    val certificate = certificateRepository.update(new Certificate(
      id,
      title,
      description,
      stored.logo,
      stored.isPermanent,
      isOpenBadgesIntegration,
      shortDescription,
      companyId,
      period,
      value,
      stored.createdAt,
      stored.isPublished,
      scope)
    )

    if (certificate.isPublished) {
      assetHelper.updateCertificateAssetEntry(certificate, Some(userId))
    }
    certificate
  }

  def changeLogo(id: Long, newLogo: String) {
    val certificate = certificateRepository.getById(id)
    certificateRepository.update(certificate.copy(logo = newLogo))
  }

  def delete(id: Long) = {
    assetHelper.deleteAssetEntry(id)
    SocialActivityLocalServiceHelper.deleteActivities(CertificateStateType.getClass.getName, id)
    SocialActivityLocalServiceHelper.deleteActivities(CertificateActivityType.getClass.getName, id)
    SocialActivityLocalServiceHelper.deleteActivities(className, id)
    certificateMemberService.delete(id)
    certificateRepository.delete(id)
    fileService.deleteByPrefix(logoPathPrefix(id))
  }

  def clone(certificateId: Long): Certificate = {
    val certificate = certificateRepository.getById(certificateId)
    val titlePattern = "copy"
    val newTitle = getTitle(certificate, titlePattern)

    val newCertificate =
      certificateRepository.create(certificate.copy(title = newTitle, isPublished = false))

    val groupIds = goalGroupRepository.get(certificateId)
      .map { gr =>
        gr.id -> goalGroupRepository.create(gr.count, newCertificate.id, gr.periodValue, gr.periodType, gr.arrangementIndex)
      }.toMap

    val goals = goalRepository.getBy(certificateId)

    // copy relationships
    courseGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(c => {
        goals.find(g => g.id == c.goalId).foreach(goalData =>
          courseGoalRepository.create(
            newCertificate.id,
            c.courseId,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    activityGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(activity => {
        goals.find(g => g.id == activity.goalId).foreach(goalData =>
          activityGoalRepository.create(
            newCertificate.id,
            activity.activityName,
            activity.count,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    statementGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(st => {
        goals.find(g => g.id == st.goalId).foreach(goalData =>
          statementGoalRepository.create(
            newCertificate.id,
            st.verb,
            st.obj,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    packageGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(p => {
        goals.find(g => g.id == p.goalId).foreach(goalData =>
          packageGoalRepository.create(
            newCertificate.id,
            p.packageId,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    assignmentGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(assignment => {
        goals.find(g => g.id == assignment.goalId).foreach(goalData =>
          assignmentGoalRepository.create(
            newCertificate.id,
            assignment.assignmentId,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    if (certificate.logo.nonEmpty) {
      val img = fileService.getFileContent(certificate.id.toString, certificate.logo)
      fileService.setFileContent(newCertificate.id.toString, certificate.logo, img)
    }
    certificateRepository.getById(newCertificate.id)
  }

  def publish(certificateId: Long, userId: Long, courseId: Long) {
    val now = DateTime.now
    val (certificate, counts) = certificateRepository.getByIdWithItemsCount(certificateId)
      .getOrElse(throw new EntityNotFoundException(s"no certificate with id: $certificateId"))

    val userStatus = counts match {
      case CertificateItemsCount(_, 0, 0, 0, 0, 0) => CertificateStatuses.Success
      case _ => CertificateStatuses.InProgress
    }

    assetHelper.updateCertificateAssetEntry(certificate)

    certificateRepository.update(certificate.copy(isPublished = true))
    certificateStatusRepository
      .getByCertificateId(certificateId)
      .foreach(s => certificateStatusRepository.update(s.copy(
        status = userStatus,
        userJoinedDate = now,
        statusAcquiredDate = now
      )))

    stateSocialActivity.addWithSet(
      certificate.companyId,
      userId,
      courseId = Some(courseId),
      classPK = Some(certificateId),
      `type` = Some(CertificateStateType.Publish.id),
      createDate = now
    )
    certificateStatusRepository.getUsersBy(certificateId).foreach { userId =>
      addPackageGoalState(certificateId, userId)
      addAssignmentGoalState(certificateId, userId)
    }
  }

  def addPackageGoalState(certificateId: Long, userId: Long) {
    goalRepository.getByCertificate(certificateId)
      .filter(_.goalType == GoalType.Package).foreach { goal =>
        val state = getPackageGoalStatus(goal, userId)
        goalStateRepository.create(
          CertificateGoalState(
            userId,
            certificateId,
            goal.id,
            state,
            DateTime.now,
            goal.isOptional)
        )
      }
  }

  private def getPackageGoalStatus(goal: CertificateGoal, userId: Long): GoalStatuses.Value = {
    packageGoalRepository.getBy(goal.id).fold(GoalStatuses.InProgress) { packageGoal =>
      val isFinished = lessonService.getLesson(packageGoal.packageId) map { lesson =>
        val grade = teacherGradeService.get(userId, lesson.id).flatMap(_.grade)
        gradeService.isLessonFinished(grade, userId, lesson)
      }
      if (isFinished.contains(true)) {
        GoalStatuses.Success
      }
      else {
        GoalStatuses.InProgress
      }
    }
  }

  def addAssignmentGoalState(certificateId: Long, userId: Long) {
    goalRepository.getByCertificate(certificateId)
      .filter(_.goalType == GoalType.Assignment).foreach { goal =>
        val state = getAssignmentGoalStatus(goal, userId)
        goalStateRepository.create(
          CertificateGoalState(
            userId,
            certificateId,
            goal.id,
            state,
            DateTime.now,
            goal.isOptional)
        )
      }
  }

  private def getAssignmentGoalStatus(goal: CertificateGoal, userId: Long): GoalStatuses.Value = {
    assignmentGoalRepository.getBy(goal.id).fold(GoalStatuses.InProgress) { assignmentGoal =>
      val isCompleted = assignmentService.getById(assignmentGoal.assignmentId) map { assignment =>
        assignmentService.getSubmissionStatus(assignment.id, userId).contains(UserStatuses.Completed)
      }
      if (isCompleted.contains(true)) {
        GoalStatuses.Success
      }
      else {
        GoalStatuses.InProgress
      }
    }
  }

  def unpublish(certificateId: Long) {
    val certificate = certificateRepository.getById(certificateId)

    assetHelper.updateCertificateAssetEntry(certificate, isVisible = false)

    certificateRepository.update(certificate.copy(isPublished = false))
    goalStateRepository.deleteBy(certificateId)
  }

  private def getIndexInTitle(title: String, titlePattern: String): Int = {
    val copyRegex = (" " + titlePattern + " (\\d+)").r
    copyRegex.findFirstMatchIn(title)
      .flatMap(str => Try(str.group(1).toInt).toOption)
      .getOrElse(0)
  }

  private def cleanTitle(title: String, titlePattern: String): String = {
    val cleanerRegex = ("(.*) " + titlePattern + " \\d+$").r
    title match {
      case cleanerRegex(text) => text.trim
      case _ => title
    }
  }

  private def getTitle(certificate: Certificate, titlePattern: String) = {
    val cleanedTitle = cleanTitle(certificate.title, titlePattern)
    val filter = CertificateFilter(certificate.companyId, Some(cleanedTitle + s" $titlePattern"))
    val certificates = certificateRepository.getBy(filter).sortBy(_.title) ++ Seq(certificate)

    val maxIndex = certificates.map(c => getIndexInTitle(c.title, titlePattern)).max
    cleanedTitle + s" $titlePattern " + (maxIndex + 1)
  }

  override def getGoals(certificateId: Long): Seq[CertificateGoal] = {
    goalRepository.getByCertificate(certificateId)
  }

  override def getGroups(certificateId: Long): Seq[GoalGroup] = {
    goalGroupRepository.get(certificateId)
  }
}
