package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.AssignmentGoal
import com.arcusys.valamis.persistence.common.DbNameUtils.tblName
import com.arcusys.valamis.persistence.common.SlickProfile

trait AssignmentGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import driver.api._

  class AssignmentGoalTable(tag: Tag)
    extends Table[AssignmentGoal](tag, tblName("CERT_GOALS_ASSIGNMENT"))
      with CertificateGoalBaseColumns {

    def assignmentId = column[Long]("ASSIGNMENT_ID")

    def * = (goalId, certificateId, assignmentId) <> (AssignmentGoal.tupled, AssignmentGoal.unapply)

    def PK = goalPK("ASSIGNMENT")
    def certificateFK = goalCertificateFK("ASSIGNMENT")
    def assignmentGoalFK = goalFK("ASSIGNMENT")
  }

  val assignmentGoals = TableQuery[AssignmentGoalTable]
}