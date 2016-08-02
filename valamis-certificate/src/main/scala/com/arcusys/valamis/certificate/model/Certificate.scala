package com.arcusys.valamis.certificate.model

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

/**
 * Certificate to be passed by user. Contains list of sites.
 *
 * @param id                  Unique internal ID
 * @param title               Short title
 * @param description         More detailed description
 */
case class Certificate(
    id: Long,
    title: String,
    description: String,
    logo: String = "",
    isPermanent: Boolean = true,
    isPublishBadge: Boolean = false,
    @deprecated
    shortDescription: String = "",
    companyId: Long,
    validPeriodType: PeriodType = PeriodTypes.UNLIMITED,
    validPeriod: Int = 0,
    createdAt: DateTime,
    isPublished: Boolean = false,
    @deprecated
    scope: Option[Long] = None) {

  def expirationDate = PeriodTypes.getEndDate(validPeriodType, validPeriod, createdAt)
}





object CertificateActionType extends Enumeration {
  type CertificateActionType = Value
  val NEW = Value(2)
  val PASSED = Value(1)
}

case class CertificateUsersStatistic(
    totalUsers: Int,
    successUsers: Int,
    failedUsers: Int,
    overdueUsers: Int
)

case class CertificateItemsCount(
    usersCount: Int,
    coursesCount: Int,
    statementsCount: Int,
    activitiesCount: Int,
    packagesCount: Int,
    assignmentsCount: Int)