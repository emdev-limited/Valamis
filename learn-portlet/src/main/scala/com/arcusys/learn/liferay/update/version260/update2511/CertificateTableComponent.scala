package com.arcusys.learn.liferay.update.version260.update2511

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.TupleHelpers._
import org.joda.time.DateTime

trait CertificateTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>

  import driver.simple._

  class CertificateTable(tag: Tag) extends LongKeyTable[Certificate](tag, "CERTIFICATE") {
    implicit val ValidPeriodTypeMapper = enumerationMapper(PeriodTypes)

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
    def isPublished = column[Boolean]("IS_PUBLISHED")
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
      isPublished,
      scope) <> (Certificate.tupled, Certificate.unapply)

    def update = (id,
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
      isPublished,
      scope) <> (Certificate.tupled, Certificate.unapply)
  }

  val certificates = TableQuery[CertificateTable]
}