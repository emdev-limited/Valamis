package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.CourseGoal
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile}

trait CourseGoalTableComponent
  extends LongKeyTableComponent
    with CertificateTableComponent
    with CertificateGoalTableComponent{ self: SlickProfile =>

  import driver.simple._

  class CourseGoalTable(tag: Tag) extends Table[CourseGoal](tag, tblName("CERT_GOALS_COURSE")) {
    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def courseId = column[Long]("COURSE_ID")

    def * = (goalId, certificateId, courseId) <> (CourseGoal.tupled, CourseGoal.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_COURSE"), (goalId, certificateId))
    def certificateFK = foreignKey(fkName("GOALS_COURSE_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
    def goalFK = foreignKey(fkName("GOALS_COURSE_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val courseGoals = TableQuery[CourseGoalTable]
}