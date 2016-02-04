package com.arcusys.learn.facades

import java.io.{File, InputStream}

import com.arcusys.learn.models.response.certificates._
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.model.{RangeResult, SkipTake}

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


  def getById(certificateId: Long): CertificateResponseContract

  def getById(certificateId: Long, userId: Long): CertificateResponseContract

  def getForSucceedUsers(companyId: Long, title: String, count: Option[Int] = None): Seq[CertificateSuccessUsersResponse]

  def create(companyId: Long,
             ownerId: Long,
             title: String,
             description: String): CertificateResponse

  def change(id: Long,
    title: String,
    description: String,
    validPeriodType: String,
    validPeriodValue: Option[Int],
    isOpenBadgesIntegration: Boolean,
    shortDescription: String = "",
    companyId: Long,
    ownerId: Long,
    scope: Option[Long]): CertificateResponseContract

  def getForUser(userId:Long,
                 companyId: Long,
                 isShortResult: Boolean,
                 sortAZ: Boolean = true,
                 skipTake: Option[SkipTake] = None,
                 titlePattern: Option[String] = None,
                 isPublished: Option[Boolean] = None): RangeResult[CertificateResponseContract]

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

  def exportCertificate(companyId: Long, certificateId: Long): InputStream

  def exportCertificates(companyId: Long): InputStream

  def importCertificates(file: File, companyId: Long): Unit

//  def getAvailableStatements(statementApi: StatementApi, page: Int, skip: Int, take: Int, filter: String,
//    isSortDirectionAsc: Boolean): CollectionResponse[AvailableStatementResponse]

  def getStatesBy(
    userId: Long,
    companyId: Long,
    statuses: Set[CertificateStatuses.Value]): Seq[AchievedCertificateStateResponse]
}
