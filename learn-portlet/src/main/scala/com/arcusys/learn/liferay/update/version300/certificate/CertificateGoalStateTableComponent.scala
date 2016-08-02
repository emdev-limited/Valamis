package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.certificate.model.goal.{GoalStatuses, GoalType}
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CertificateGoalStateTableComponent
  extends CertificateTableComponent
    with TypeMapper { self: SlickProfile =>
  import driver.simple._

  case class CertificateGoalState(id: Option[Long],
                                  userId: Long,
                                  certificateId: Long,
                                  goalId: Long,
                                  goalType: String,
                                  status: GoalStatuses.Value,
                                  modifiedDate: DateTime,
                                  isOptional: Boolean)

  class CertificateGoalStateTable(tag: Tag)  extends Table[CertificateGoalState](tag, tblName("CERT_GOAL_STATE")) {
    implicit lazy val certificateGoalStatusTypeMapper = enumerationMapper(GoalStatuses)
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def userId = column[Long]("USER_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalId = column[Long]("GOAL_ID")
    def goalType = column[String]("GOAL_TYPE")
    def status = column[GoalStatuses.Value]("STATUS")
    def modifiedDate = column[DateTime]("MODIFIED_DATE")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (id.?, userId, certificateId, goalId, goalType, status, modifiedDate, isOptional) <> (CertificateGoalState.tupled, CertificateGoalState.unapply)

  }

  val certificateGoalStates = TableQuery[CertificateGoalStateTable]
}
