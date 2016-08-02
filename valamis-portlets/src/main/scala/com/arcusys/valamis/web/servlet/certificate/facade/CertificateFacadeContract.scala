package com.arcusys.valamis.web.servlet.certificate.facade

import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.web.servlet.certificate.response._

trait CertificateFacadeContract {

  def getGoalsStatuses(certificateId: Long, userId: Long): GoalsStatusResponse

  def getCountGoals(certificateId: Long, userId: Long): Int

  def getGoalsDeadlines(certificateId: Long, userId: Long): GoalsDeadlineResponse

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          sortAZ: Boolean,
                                          isShortResult: Boolean,
                                          skipTake: Option[SkipTake],
                                          titlePattern: Option[String]
                                           ): RangeResult[CertificateResponseContract]

  def getForSucceedUsers(companyId: Long, title: String, count: Option[Int] = None): Seq[CertificateSuccessUsersResponse]

  def getForUserWithStatus(userId: Long,
                           companyId: Long,
                           sortAZ: Boolean,
                           skipTake: Option[SkipTake],
                           titlePattern: Option[String],
                           isPublished: Option[Boolean]): RangeResult[CertificateWithUserStatusResponse]

  def getAvailableForUser(companyId: Long,
    skipTake: Option[SkipTake],
    titlePattern: Option[String],
    sortAZ: Boolean,
    userId: Long,
    isShortResult: Boolean,
    scope: Option[Long]): RangeResult[CertificateResponseContract]

  def getStatesBy(
    userId: Long,
    companyId: Long,
    scopeId: Option[Long],
    statuses: Set[CertificateStatuses.Value]): Seq[AchievedCertificateStateResponse]
}
