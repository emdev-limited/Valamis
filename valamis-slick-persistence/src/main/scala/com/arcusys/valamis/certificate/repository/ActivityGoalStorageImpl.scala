package com.arcusys.valamis.certificate.repository

import com.arcusys.valamis.certificate.model.goal.ActivityGoal
import com.arcusys.valamis.certificate.schema.ActivityGoalTableComponent
import com.arcusys.valamis.certificate.storage.ActivityGoalStorage
import com.arcusys.valamis.core.SlickProfile
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class ActivityGoalStorageImpl (val db:     JdbcBackend#DatabaseDef,
                               val driver: JdbcProfile)
  extends ActivityGoalStorage
  with ActivityGoalTableComponent
  with SlickProfile {

  import driver.simple._

  def create(certificateId: Long, activityName: String, count: Int, periodValue: Int, periodType: PeriodType) =
    db.withSession { implicit session =>
      val activityGoal = ActivityGoal(certificateId.toInt, activityName, count, periodValue, periodType)
      activityGoals.insert(activityGoal)

      activityGoals
        .filter(ag => ag.certificateId === activityGoal.certificateId && ag.activityName === activityGoal.activityName)
        .first
    }

  def modify(certificateId: Long, activityName: String, count: Int, periodValue: Int, periodType: PeriodType) =
    db.withSession{ implicit session =>
      val filtered = activityGoals.filter(entity => entity.certificateId === certificateId && entity.activityName === activityName)

      filtered.update(ActivityGoal(certificateId.toInt, activityName, count, periodValue, periodType))
      filtered.first
    }

  override def getByCertificateId(certificateId: Long): Seq[ActivityGoal] = db.withSession { implicit session =>
    activityGoals.filter(_.certificateId === certificateId).run
  }

  override def getByCertificateIdCount(certificateId: Long): Int = db.withSession { implicit session =>
    activityGoals.filter(_.certificateId === certificateId).length.run
  }

  override def get(certificateId: Long, activityName: String) = db.withSession { implicit session =>
    activityGoals.filter(ag => ag.certificateId === certificateId && ag.activityName === activityName).firstOption
  }

  override def delete(certificateId: Long, activityName: String) = db.withSession { implicit session =>
    activityGoals.filter(ag => ag.certificateId === certificateId && ag.activityName === activityName).delete
  }
}
