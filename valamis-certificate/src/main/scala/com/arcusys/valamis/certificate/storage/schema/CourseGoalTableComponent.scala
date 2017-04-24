package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.CourseGoal
import com.arcusys.valamis.persistence.common.DbNameUtils.tblName
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile}

trait CourseGoalTableComponent
  extends LongKeyTableComponent
    with CertificateTableComponent
    with CertificateGoalTableComponent{ self: SlickProfile =>

  import driver.simple._

  class CourseGoalTable(tag: Tag)
    extends Table[CourseGoal](tag, tblName("CERT_GOALS_COURSE"))
      with CertificateGoalBaseColumns {

    def courseId = column[Long]("COURSE_ID")

    def * = (goalId, certificateId, courseId) <> (CourseGoal.tupled, CourseGoal.unapply)

    def PK = goalPK("COURSE")
    def certificateFK = goalCertificateFK("COURSE")
    def courseGoalFK = goalFK("COURSE")
  }

  val courseGoals = TableQuery[CourseGoalTable]
}