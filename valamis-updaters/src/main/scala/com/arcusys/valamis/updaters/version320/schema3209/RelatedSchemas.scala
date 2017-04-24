package com.arcusys.valamis.updaters.version320.schema3209

import com.arcusys.valamis.persistence.common.DbNameUtils.{fkName, idName, tblName}
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CertificateTableComponent  extends TypeMapper { self: SlickProfile =>

  import driver.api._

  class CertificateTable(tag: Tag) extends Table[Certificate](tag, tblName("CERTIFICATE")) {

    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION", O.Length(2000, varying = true))
    def logo = column[String]("LOGO")
    def isPermanent = column[Boolean]("IS_PERMANENT")
    def isPublishBadge = column[Boolean]("IS_PUBLISH_BADGE")
    def shortDescription = column[String]("SHORT_DESCRIPTION")
    def companyId = column[Long]("COMPANY_ID")
    def validPeriodType = column[String]("PERIOD_TPE")
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

trait CertificateGoalTableComponent
    extends TypeMapper
    with CertificateTableComponent { self: SlickProfile =>

  import driver.api._

  class CertificateGoalTable(tag: Tag)
    extends Table[CertificateGoal](tag, tblName("CERT_GOALS")) {

    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)
    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalType = column[Int]("GOAL_TYPE")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[String]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")
    def groupId = column[Option[Long]]("GROUP_ID")
    def oldGroupId = column[Option[Long]]("OLD_GROUP_ID")
    def modifiedDate = column[DateTime]("MODIFIED_DATE")
    def userId = column[Option[Long]]("USER_ID")
    def isDeleted = column[Boolean]("IS_DELETED")

    def * = (id,
      certificateId,
      goalType,
      periodValue,
      periodType,
      arrangementIndex,
      isOptional,
      groupId,
      oldGroupId,
      modifiedDate,
      userId,
      isDeleted) <> (CertificateGoal.tupled, CertificateGoal.unapply)


    def certificateFK = foreignKey(fkName("GOALS_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
  }

  val certificateGoals = TableQuery[CertificateGoalTable]
}