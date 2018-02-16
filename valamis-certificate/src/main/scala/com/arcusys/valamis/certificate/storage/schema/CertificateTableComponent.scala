package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import org.joda.time.DateTime
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

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

    def update = (
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
      scope) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val certificates = TableQuery[CertificateTable]
}