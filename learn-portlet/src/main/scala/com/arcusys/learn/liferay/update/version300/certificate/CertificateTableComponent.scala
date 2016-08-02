package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CertificateTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.simple._

  case class Certificate(id: Long,
                         title: String,
                         description: String,
                         logo: String = "",
                         isPermanent: Boolean = true,
                         isPublishBadge: Boolean = false,
                         shortDescription: String = "",
                         companyId: Long,
                         validPeriodType: PeriodType = PeriodTypes.UNLIMITED,
                         validPeriod: Int = 0,
                         createdAt: DateTime,
                         isPublished: Boolean = false,
                         scope: Option[Long] = None,
                         optionalGoals: Option[Int] = None)

  class CertificateTable(tag: Tag) extends Table[Certificate](tag, tblName("CERTIFICATE")) {

    implicit val ValidPeriodTypeMapper = MappedColumnType.base[PeriodTypes.PeriodType, String](
      s => s.toString,
      s => PeriodTypes.withName(s)
    )

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION")
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
    def optionalGoals = column[Option[Int]]("OPTIONAL_GOALS")

    def * = (id,
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
      scope,
      optionalGoals) <>(Certificate.tupled, Certificate.unapply)
  }

  val certificates = TableQuery[CertificateTable]
}