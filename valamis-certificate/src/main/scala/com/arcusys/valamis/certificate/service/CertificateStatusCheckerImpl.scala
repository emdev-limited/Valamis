package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.{PermissionHelper, SocialActivityLocalServiceHelper}
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.grade.storage.CourseGradeStorage
import com.arcusys.valamis.lesson.service.{ValamisPackageService, LessonStatementReader}
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.model.PeriodTypes
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

class CertificateStatusCheckerImpl(
    implicit val bindingModule: BindingModule)
  extends CertificateStatusChecker
  with CourseGoalStatusCheckerComponent
  with StatementGoalStatusCheckerComponent
  with PackageGoalStatusCheckerComponent
  with ActivityGoalStatusCheckerComponent
  with Injectable {

  protected lazy val certificateStorage = inject[CertificateRepository]
  protected lazy val courseGoalStorage = inject[CourseGoalStorage]
  protected lazy val activityGoalStorage = inject[ActivityGoalStorage]
  protected lazy val statementGoalStorage = inject[StatementGoalStorage]
  protected lazy val packageGoalStorage = inject[PackageGoalStorage]
  protected lazy val courseGradeStorage = inject[CourseGradeStorage]
  protected lazy val certificateStateRepository = inject[CertificateStateRepository]
  protected lazy val lrsClient = inject[LrsClientManager]
  protected lazy val statementReader = inject[LessonStatementReader]
  protected lazy val packageService = inject[ValamisPackageService]

  override def checkAndGetStatus(filter: CertificateStateFilter): Seq[CertificateState] = {

    certificateStateRepository.getBy(filter)
      .map(s => (s.certificateId, s.userId))
      .foreach(s => checkAndGetStatus(s._1, s._2))

    certificateStateRepository.getBy(filter)
  }

  override def checkAndGetStatus(certificateFilter: CertificateFilter, stateFilter: CertificateStateFilter): Seq[CertificateState] = {
    certificateStateRepository.getBy(stateFilter, certificateFilter)
      .map(s => (s.certificateId, s.userId))
      .distinct
      .foreach(s => checkAndGetStatus(s._1, s._2))

    certificateStateRepository.getBy(stateFilter, certificateFilter)
  }

  override def checkAndGetStatus(certificateId: Long, userId: Long): CertificateStatuses.Value = {
    PermissionHelper.preparePermissionChecker(userId)

    val certificate = certificateStorage.getById(certificateId)
    val certificateState = certificateStateRepository.getBy(userId, certificateId).get

    if(!certificate.isPublished) CertificateStatuses.InProgress
    else
      certificateState.status match {
        case CertificateStatuses.Failed => CertificateStatuses.Failed
        case CertificateStatuses.Overdue => CertificateStatuses.Overdue
        case CertificateStatuses.InProgress => checkInProgress(certificate, certificateState)
        case CertificateStatuses.Success => checkSuccess(certificate, certificateState)
      }
  }

  private def checkInProgress(certificate: Certificate, certificateState: CertificateState) = {
    val socialActivities = SocialActivityLocalServiceHelper.getActivities(certificateState.userId, certificateState.userJoinedDate)

    val courseGoals = courseGoalStorage.getByCertificateId(certificate.id).toStream
    val activityGoals = activityGoalStorage.getByCertificateId(certificate.id).toStream
    val statementGoals = statementGoalStorage.getByCertificateId(certificate.id).toStream
    val packageGoals = packageGoalStorage.getByCertificateId(certificate.id).toStream

    val goalStatuses =
      activityGoals.map(checkActivityGoal(certificateState.userId, socialActivities, certificateState.userJoinedDate)) ++
        statementGoals.map(checkStatementGoal(certificateState.userId, certificateState.userJoinedDate)) ++
        packageGoals.map(checkPackageGoal(certificateState.userId, certificateState.userJoinedDate)) ++
        courseGoals.map(checkCourseGoal(certificateState.userId, certificateState.userJoinedDate))

    if (goalStatuses.contains(GoalStatuses.Failed)) {
      updateState(certificateState, CertificateStatuses.Failed, certificate.companyId)
      CertificateStatuses.Failed
    }
    else if (goalStatuses.contains(GoalStatuses.InProgress)) {
      CertificateStatuses.InProgress
    }
    else {
      updateState(certificateState, CertificateStatuses.Success, certificate.companyId)
      CertificateStatuses.Success
    }
  }

  private def checkSuccess(certificate: Certificate, certificateState: CertificateState) = {
    val certificateExpirationDate =
      PeriodTypes.getEndDate(
        certificate.validPeriodType,
        certificate.validPeriod,
        certificateState.statusAcquiredDate)

    if (certificateExpirationDate.isAfterNow) CertificateStatuses.Success
    else {
      updateState(certificateState, CertificateStatuses.Overdue, certificate.companyId)
      CertificateStatuses.Overdue
    }
  }

  private def updateState(certificateState: CertificateState, status: CertificateStatuses.Value, companyId: Long) = {
    certificateStateRepository.update(certificateState.copy(status = status, statusAcquiredDate = DateTime.now))
    SocialActivityLocalServiceHelper.addWithSet(
      companyId,
      certificateState.userId,
      classOf[Certificate].getName,
      classPK = Some(certificateState.certificateId),
      `type` = Some(status.id))
  }
}
