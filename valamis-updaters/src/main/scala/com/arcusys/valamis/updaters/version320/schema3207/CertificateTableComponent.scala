package com.arcusys.valamis.updaters.version320.schema3207

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent3, SlickProfile, TypeMapper}
import com.arcusys.valamis.updaters.common.model.PeriodTypes
import com.arcusys.valamis.updaters.common.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

trait CertificateTableComponent extends LongKeyTableComponent3 with TypeMapper {
  self: SlickProfile =>

  import driver.api._

  case class Certificate(id: Long,
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
                         activationDate: Option[DateTime] = None,
                         isActive: Boolean = false,
                         @deprecated
                         scope: Option[Long] = None)

  class CertificateTable(tag: Tag) extends Table[Certificate](tag, tblName("CERTIFICATE")) {

    implicit val ValidPeriodTypeMapper = enumerationMapper(PeriodTypes)


    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def title = column[String]("TITLE")

    def description = column[String]("DESCRIPTION", O.Length(2000, varying = true))

    def logo = column[String]("LOGO")

    def isPermanent = column[Boolean]("IS_PERMANENT")

    def isPublishBadge = column[Boolean]("IS_PUBLISH_BADGE")

    def shortDescription = column[String]("SHORT_DESCRIPTION")

    def companyId = column[Long]("COMPANY_ID")

    def validPeriodType = column[PeriodTypes.PeriodType]("PERIOD_TPE")

    def validPeriod = column[Int]("VALID_PERIOD")

    def createdAt = column[DateTime]("CREATED_AT")

    def activationDate = column[Option[DateTime]]("ACTIVATION_DATE")

    def isActive = column[Boolean]("IS_ACTIVE")

    def scope = column[Option[Long]]("SCOPE")

    def * = (
      id,
      title,
      description,
      logo,
      isPermanent,
      isPublishBadge,
      shortDescription,
      companyId,
      validPeriodType,
      validPeriod,
      createdAt,
      activationDate,
      isActive,
      scope) <> (Certificate.tupled, Certificate.unapply)
  }

  val certificates = TableQuery[CertificateTable]
}