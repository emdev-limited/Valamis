package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.ActivityGoal
import com.arcusys.valamis.persistence.common.DbNameUtils.tblName
import com.arcusys.valamis.persistence.common.SlickProfile

trait ActivityGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import driver.simple._

  class ActivityGoalTable(tag: Tag)
    extends Table[ActivityGoal](tag, tblName("CERT_GOALS_ACTIVITY"))
      with CertificateGoalBaseColumns {

    def activityName = column[String]("ACTIVITY_NAME", O.Length(254, varying = true))
    def count = column[Int]("COUNT")

    def * = (goalId, certificateId, activityName, count) <> (ActivityGoal.tupled, ActivityGoal.unapply)

    def PK = goalPK("ACTIVITY")
    def certificateFK = goalCertificateFK("ACTIVITY")
    def activityGoalFK = goalFK("ACTIVITY")
  }

  val activityGoals = TableQuery[ActivityGoalTable]
}