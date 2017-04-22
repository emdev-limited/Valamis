package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{GoalType, StatementGoal}
import com.arcusys.valamis.certificate.storage.schema.StatementGoalTableComponent
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, StatementGoalStorage}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

abstract class StatementGoalStorageImpl (val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends StatementGoalStorage
    with StatementGoalTableComponent
    with SlickProfile
    with DatabaseLayer
    with Queries {

  import driver.api._

  def certificateGoalRepository: CertificateGoalRepository

  private def getStatementAction(certificateId: Long, verb: String, obj: String, isDeleted: Option[Boolean]) = {
    statementGoals
      .filterByCertificateId(certificateId)
      .filter(sg => sg.verb === verb && sg.obj === obj)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }

  override def get(certificateId: Long, verb: String, obj: String, isDeleted: Option[Boolean]): Option[StatementGoal] =
    execSync(getStatementAction(certificateId, verb, obj, isDeleted).headOption)

  override def getBy(goalId: Long): Option[StatementGoal] = execSync {
    statementGoals.filterByGoalId(goalId).result.headOption
  }

  override def create(certificateId: Long,
                      verb: String,
                      obj: String,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): StatementGoal = {

    val deletedGoal = get(certificateId, verb, obj, isDeleted = Some(true))

    val insertOrUpdate = deletedGoal map { goal =>
      val certificateGoal = certificateGoalRepository.getById(goal.goalId, isDeleted = Some(true))
      certificateGoalRepository.modify(
        goal.goalId,
        certificateGoal.periodValue,
        certificateGoal.periodType,
        certificateGoal.arrangementIndex,
        isOptional = false,
        groupId = None,
        oldGroupId = None,
        userId = None,
        isDeleted = false
      )
      DBIO.successful()
    } getOrElse {
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

      statementGoals += statementGoal
    }

    val resultAction = getStatementAction(certificateId, verb, obj, Some(false)).head

    execSyncInTransaction(insertOrUpdate >> resultAction)
  }

  override def getByVerbAndObj(verb: String, obj: String): Seq[StatementGoal] = execSync {
    statementGoals
      .filter(sg => sg.verb === verb && sg.obj === obj)
      .result
  }

  override def getByCertificateId(certificateId: Long, isDeleted: Option[Boolean]): Seq[StatementGoal] = {
    execSync {
      statementGoals
        .filterByCertificateId(certificateId)
        .filterByDeleted(isDeleted)
        .map(_._1)
        .result
    }
  }
}