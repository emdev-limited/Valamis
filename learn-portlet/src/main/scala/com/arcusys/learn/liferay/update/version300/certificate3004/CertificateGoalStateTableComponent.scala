package com.arcusys.learn.liferay.update.version300.certificate3004

import com.arcusys.valamis.certificate.model.goal.GoalStatuses
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CertificateGoalStateTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent
    with TypeMapper { self: SlickProfile =>
  import driver.simple._

  case class CertificateGoalState(userId: Long,
                                  certificateId: Long,
                                  goalId: Long,
                                  status: GoalStatuses.Value,
                                  modifiedDate: DateTime,
                                  isOptional: Boolean)

  implicit lazy val certificateGoalStatusTypeMapper = enumerationMapper(GoalStatuses)

  class CertificateGoalStateTable(tag: Tag) extends Table[CertificateGoalState](tag, tblName("CERT_GOALS_STATE")) {
    def userId = column[Long]("USER_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalId = column[Long]("GOAL_ID")
    def status = column[GoalStatuses.Value]("STATUS")
    def modifiedDate = column[DateTime]("MODIFIED_DATE")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (userId, certificateId, goalId, status, modifiedDate, isOptional) <> (CertificateGoalState.tupled, CertificateGoalState.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_STATE"), (userId, goalId))

    def certificateFK = foreignKey(fkName("CERT_GOALS_STATE_TO_SERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)

    def goalFK = foreignKey(fkName("CERT_GOALS_STATE_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)

  }

  val certificateGoalStates = TableQuery[CertificateGoalStateTable]
}
