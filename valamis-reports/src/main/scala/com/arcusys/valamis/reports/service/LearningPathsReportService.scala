package com.arcusys.valamis.reports.service

import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.model.{Certificate, CertificateFilter, CertificateSortBy, CertificateStatuses}
import com.arcusys.valamis.certificate.service.{CertificateGoalService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage.{CertificateGoalStateRepository, CertificateRepository, CertificateStateRepository}
import com.arcusys.valamis.model.{Order, PeriodTypes, SkipTake}
import com.arcusys.valamis.reports.model.{PathGoalReportResult, PathReportDetailedResult, PathReportResult, PathsReportStatus}
import org.joda.time.DateTime


trait LearningPathsReportService {
  def getCertificates(companyId: Long, courseId: Option[Long]): Seq[(Certificate, Seq[CertificateGoal])]

  def getStatuses(companyId: Long,
                  courseId: Option[Long],
                  userIds: Seq[Long],
                  skipTake: SkipTake): Map[Long, Seq[PathReportResult]]

  def getJoinedCount(companyId: Long,
                     courseId: Option[Long],
                     userIds: Seq[Long]): Long

  def getTotalStatus(companyId: Long,
                     courseId: Option[Long],
                     userIds: Seq[Long]): Seq[(Certificate, Map[PathsReportStatus.Value, Int])]

  def getGoalsStatus(certificateId: Long,
                     userIds: Seq[Long]): Seq[PathReportDetailedResult]

  def getTotalGoalsStatus(certificateId: Long,
                          userIds: Seq[Long]): Seq[(Long, Map[PathsReportStatus.Value, Int])]
}


abstract class LearningPathsReportServiceImpl extends LearningPathsReportService {

  val certificateToUserRepository: CertificateStateRepository
  val certificateRepository: CertificateRepository
  val goalService: CertificateGoalService
  val certificateStatusService: CertificateStatusChecker
  val userGoalStatusRepository: CertificateGoalStateRepository

  /**
    * show expiring status if certificate end date in {expiringTimeout} days
    */
  val expiringTimeout = 30

  def getCertificates(companyId: Long, courseId: Option[Long]): Seq[(Certificate, Seq[CertificateGoal])] = {
    getCertificatesByScope(companyId, courseId)
      .map(c => (c, getOrderedGoals(c.id)))
  }

  private def getCertificatesByScope(companyId: Long, courseId: Option[Long]): Seq[Certificate] = {
    certificateRepository.getBy(
      filter = CertificateFilter(
        companyId,
        titlePattern = None,
        scope = courseId,
        isActive = Some(true),
        Some(CertificateSort(CertificateSortBy.Name, Order(true)))
      ),
      skipTake = None
    )
  }

  private def getOrderedGoals(certificateId: Long): Seq[CertificateGoal] = {
    //TODO: use group in sorting
    goalService.getGoals(certificateId)
      .filterNot(_.isDeleted)
      .sortBy(_.arrangementIndex)
  }

  def getTotalStatus(companyId: Long,
                     courseId: Option[Long],
                     userIds: Seq[Long]): Seq[(Certificate, Map[PathsReportStatus.Value, Int])] = {

    getCertificatesByScope(companyId, courseId)
      .map { certificate =>
        val total = userIds
          .map { userId => getStatus(certificate, userId) }
          .map { result => result.status }
          .groupBy(identity).mapValues(_.size)

        (certificate, total)
      }
  }

  private def getStatus(certificate: Certificate, userId: Long): PathReportResult = {
    certificateToUserRepository.getBy(userId, certificate.id) match {

      case None =>
        PathReportResult(certificate.id, userId, PathsReportStatus.Empty)

      case Some(userResult) =>
        val endDate = userResult.status match {
          case CertificateStatuses.Failed | CertificateStatuses.InProgress => None
          case _ => PeriodTypes.getEndDateOption(
            certificate.validPeriodType,
            certificate.validPeriod,
            userResult.statusAcquiredDate
          )
        }

        val status = userResult.status match {
          case CertificateStatuses.Failed => PathsReportStatus.Failed
          case CertificateStatuses.InProgress => PathsReportStatus.InProgress
          case CertificateStatuses.Overdue => PathsReportStatus.Expired

          case CertificateStatuses.Success =>
            endDate
              .filterNot(_.minusDays(expiringTimeout).isAfterNow)
              .map(_ => PathsReportStatus.Expiring)
              .getOrElse(PathsReportStatus.Achieved)
        }

        PathReportResult(certificate.id, userId, status, Option(userResult.statusAcquiredDate), endDate)
    }
  }

  def getStatuses(companyId: Long,
                  courseId: Option[Long],
                  userIds: Seq[Long],
                  skipTake: SkipTake): Map[Long, Seq[PathReportResult]] = {

    val certificates = getCertificatesByScope(companyId, courseId)

    userIds
      .filter { userId =>
        certificates.exists(c => certificateToUserRepository.getBy(userId, c.id).isDefined)
      }
      .slice(skipTake.skip, skipTake.skip + skipTake.take)
      .map { userId =>
        val statuses = certificates.map(getStatus(_, userId))
        (userId, statuses)
      }
      .toMap
  }

  def getJoinedCount(companyId: Long,
                     courseId: Option[Long],
                     userIds: Seq[Long]): Long = {

    val certificates = getCertificatesByScope(companyId, courseId)

    userIds.count { userId =>
      certificates.exists(c => certificateToUserRepository.getBy(userId, c.id).isDefined)
    }
  }

  def getTotalGoalsStatus(certificateId: Long,
                          userIds: Seq[Long]): Seq[(Long, Map[PathsReportStatus.Value, Int])] = {
    val statistics = getGoalsStatus(certificateId, userIds)
      .flatMap(_.goals)

    statistics
      .groupBy(_.goalId)
      .map { case (goal, statuses) =>
        val total = statuses.groupBy(_.status).map { case (k, v) => (k, v.size) }
        (goal, total)
      }
      .toSeq
  }

  def getGoalsStatus(certificateId: Long,
                     userIds: Seq[Long]): Seq[PathReportDetailedResult] = {

    userIds.flatMap { userId =>

      certificateToUserRepository.getBy(userId, certificateId) match {
        case None => Nil // we can't check goals if user not joined
        case Some(userState) =>
          val goalStatuses = getGoalsStatuses(certificateId, userId, userState.statusAcquiredDate)

          Seq(PathReportDetailedResult(certificateId, userId, goalStatuses))
      }
    }
  }

  private def getGoalsStatuses(certificateId: Long, userId: Long, userJoinDate: DateTime): Seq[PathGoalReportResult] = {
    val courseGoals = certificateStatusService.getCourseGoalsStatus(certificateId, userId)
      .map(c => (c.goal.goalId, c.status))

    val activityGoals = certificateStatusService.getActivityGoalsStatus(certificateId, userId)
      .map(c => (c.goal.goalId, c.status))

    val statementGoals = certificateStatusService.getStatementGoalsStatus(certificateId, userId)
      .map(c => (c.goal.goalId, c.status))

    val packageGoals = certificateStatusService.getPackageGoalsStatus(certificateId, userId)
      .map(c => (c.goal.goalId, c.status))

    val assignmentGoals = certificateStatusService.getAssignmentGoalsStatus(certificateId, userId)
      .map(c => (c.goal.goalId, c.status))

    val statuses = (courseGoals ++ activityGoals ++ statementGoals ++ packageGoals ++ assignmentGoals)
      .map { case (goalId, status) =>

        val statusDate = userGoalStatusRepository.getBy(userId, goalId).map(_.modifiedDate)
          .getOrElse(userJoinDate)

        val reportStatus = status match {
          case GoalStatuses.InProgress => PathsReportStatus.InProgress
          case GoalStatuses.Failed => PathsReportStatus.Failed
          case GoalStatuses.Success => PathsReportStatus.Achieved
        }

        PathGoalReportResult(goalId, statusDate, reportStatus)
      }

    getOrderedGoals(certificateId).flatMap { goal =>
      statuses.find(_.goalId == goal.id)
    }
  }

}
