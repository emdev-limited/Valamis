package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.AssignmentGoal
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait AssignmentGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import driver.api._

  class AssignmentGoalTable(tag: Tag) extends Table[AssignmentGoal](tag, tblName("CERT_GOALS_ASSIGNMENT")) {
    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def assignmentId = column[Long]("ASSIGNMENT_ID")

    def * = (goalId, certificateId, assignmentId) <> (AssignmentGoal.tupled, AssignmentGoal.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_ASSIGNMENT"), (certificateId, assignmentId))
    def certificateFK = foreignKey(fkName("GOALS_ASSIGNMENT_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
    def goalFK = foreignKey(fkName("GOALS_ASSIGNMENT_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val assignmentGoals = TableQuery[AssignmentGoalTable]
}