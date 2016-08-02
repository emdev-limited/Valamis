package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.{CertificateGoalState, GoalStatuses}
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CertificateGoalStateTableComponent
  extends LongKeyTableComponent
    with TypeMapper
    with CertificateTableComponent
    with CertificateGoalTableComponent{ self: SlickProfile =>
  import driver.simple._

  implicit lazy val goalStatusTypeMapper = enumerationMapper(GoalStatuses)

  class CertificateGoalStateTable(tag: Tag) extends Table[CertificateGoalState](tag, tblName("CERT_GOALS_STATE")) {
    def userId = column[Long]("USER_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalId = column[Long]("GOAL_ID")
    def status = column[GoalStatuses.Value]("STATUS")
    def modifiedDate = column[DateTime]("MODIFIED_DATE")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (userId, certificateId, goalId, status, modifiedDate, isOptional) <> (CertificateGoalState.tupled, CertificateGoalState.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_STATE"), (userId, goalId))
    def certificateFK = foreignKey(fkName("CERT_GOALS_STATE_TO_SERT"), certificateId, certificates)(x => x.id)
    def goalFK = foreignKey(fkName("CERT_GOALS_STATE_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val certificateGoalStates = TableQuery[CertificateGoalStateTable]
}
