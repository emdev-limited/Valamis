package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{GoalType, StatementGoal}
import com.arcusys.valamis.certificate.storage.schema.StatementGoalTableComponent
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, StatementGoalStorage}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class StatementGoalStorageImpl (val db: JdbcBackend#DatabaseDef,
                                          val driver: JdbcProfile)
  extends StatementGoalStorage
    with StatementGoalTableComponent
  with SlickProfile {

  import driver.simple._

  def certificateGoalRepository: CertificateGoalRepository

  override def get(certificateId: Long, verb: String, obj: String) = db.withSession { implicit session =>
    statementGoals.filter(ag => ag.certificateId === certificateId && ag.verb === verb && ag.obj === obj).firstOption
  }

  override def getBy(goalId: Long): Option[StatementGoal] = db.withTransaction { implicit session =>
    statementGoals.filter(_.goalId === goalId).firstOption
  }

  override def create(certificateId: Long,
                      verb: String,
                      obj: String,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): StatementGoal = {
    db.withTransaction { implicit session =>
      val goalId = certificateGoalRepository.create(
        certificateId,
        GoalType.Statement,
        periodValue,
        periodType,
        arrangementIndex,
        isOptional,
        groupId)

      val statementGoal = StatementGoal(
        goalId,
        certificateId,
        verb,
        obj)

      statementGoals insert statementGoal

      statementGoals
        .filter(ag => ag.certificateId === statementGoal.certificateId &&
          ag.verb === statementGoal.verb &&
          ag.obj === statementGoal.obj)
        .first
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
}