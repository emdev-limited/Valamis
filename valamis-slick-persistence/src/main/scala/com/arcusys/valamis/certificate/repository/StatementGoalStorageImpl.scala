package com.arcusys.valamis.certificate.repository

import javax.inject.Inject

import com.arcusys.valamis.certificate.model.goal.StatementGoal
import com.arcusys.valamis.certificate.schema.StatementGoalTableComponent
import com.arcusys.valamis.certificate.storage.StatementGoalStorage
import com.arcusys.valamis.core.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.model.PeriodTypes._
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class StatementGoalStorageImpl @Inject() (val db: JdbcBackend#DatabaseDef,
                                          val driver: JdbcProfile)
  extends StatementGoalStorage
  with StatementGoalTableComponent
  with SlickProfile {

  import driver.simple._

  override def get(certificateId: Long, verb: String, obj: String) = db.withSession { implicit session =>
    statementGoals.filter(ag => ag.certificateId === certificateId && ag.verb === verb && ag.obj === obj).firstOption
  }

  override def delete(certificateId: Long, verb: String, obj: String) = db.withSession { implicit session =>
    statementGoals
      .filter(ag => ag.certificateId === certificateId && ag.verb === verb && ag.obj === obj)
      .delete
  }

  override def create(certificateId: Long, verb: String, obj: String, periodValue: Int, periodType: PeriodType): StatementGoal = {
    val statementGoal = StatementGoal(certificateId, verb,obj, periodValue, periodType)
    db.withSession { implicit session =>
      statementGoals.insert(statementGoal)

      statementGoals
        .filter(ag => ag.certificateId === certificateId && ag.verb === verb && ag.obj === obj)
        .first
    }
  }

  override def modify(certificateId: Long, verb: String, obj: String, periodValue: Int, periodType: PeriodType): StatementGoal = {
    val statementGoal = StatementGoal(certificateId, verb,obj, periodValue, periodType)
    db.withSession { implicit session =>
      val filtered =
        statementGoals
          .filter(entity => entity.certificateId === certificateId && entity.verb === verb && entity.obj === obj)

      filtered.update(statementGoal)
      filtered.first
    }
  }

  override def getByVerbAndObj(verb: String, obj: String): Seq[StatementGoal] = db.withSession { implicit session =>
    statementGoals
      .filter(sg => sg.verb === verb && sg.obj === obj)
      .run
  }

  override def getByCertificateId(certificateId: Long): Seq[StatementGoal] = db.withSession { implicit session =>
    statementGoals
      .filter(_.certificateId === certificateId)
      .run
  }

  override def getByCertificateIdCount(certificateId: Long): Int = db.withSession { implicit session =>
    statementGoals
      .filter(_.certificateId === certificateId)
      .length
      .run
  }
}
