package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{CertificateGoal, GoalType}
import com.arcusys.valamis.certificate.storage.CertificateGoalRepository
import com.arcusys.valamis.certificate.storage.schema.{CertificateGoalGroupTableComponent, CertificateGoalTableComponent}
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

class CertificateGoalRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateGoalRepository
  with CertificateGoalTableComponent
  with CertificateGoalGroupTableComponent
  with Queries
  with SlickProfile
  with DatabaseLayer {

  import driver.api._

  private def now = DateTime.now

  override def getById(id: Long, isDeleted: Option[Boolean]): CertificateGoal = execSync {
    certificateGoals
      .filterById(id)
      .filterByDeleted(isDeleted)
      .result.head
  }

  override def getGoalsCount(certificateId: Long, isDeleted: Option[Boolean]): Int = execSync {
    certificateGoals
      .filterByCertificateId(certificateId)
      .filterByDeleted(isDeleted)
      .size.result
  }

  override def getByCertificate(certificateId: Long,
                                isDeleted: Option[Boolean]): Seq[CertificateGoal] = execSync {

    certificateGoals
      .filterByCertificateId(certificateId)
      .filterByDeleted(isDeleted)
      .result
  }

  override def getByGroupId(groupId: Long,
                            isDeleted: Option[Boolean]): Seq[CertificateGoal] = execSync {

    certificateGoals
      .filterByGroupId(groupId)
      .filterByDeleted(isDeleted)
      .result
  }

  override def getIdsByGroup(groupId: Long): Seq[Long] = execSync {
    certificateGoals
      .filterByGroupId(groupId)
      .filter(!_.isDeleted)
      .map(_.id)
      .result
  }

  override def deleteByGroup(groupId: Long, userId: Option[Long]): Unit = execSyncInTransaction {
    certificateGoals
      .filterByGroupId(groupId)
      .map(goal => (goal.modifiedDate, goal.userId, goal.isDeleted))
      .update(DateTime.now, userId, true)
  }

  override def create(certificateId: Long,
                      goalType: GoalType.Value,
                      periodValue: Int,
                      periodType: PeriodType,
                      arrangementIndex: Int,
                      isOptional: Boolean,
                      groupId: Option[Long] = None): Long = execSyncInTransaction {

    certificateGoals
      .map(g => (g.certificateId,
        g.goalType,
        g.periodValue,
        g.periodType,
        g.arrangementIndex,
        g.isOptional,
        g.groupId,
        g.modifiedDate,
        g.isDeleted))
      .returning(certificateGoals.map(_.id)) +=
      (certificateId, goalType, periodValue, periodType, arrangementIndex, isOptional, groupId, now, false)
  }

  override def modify(goalId: Long,
                      periodValue: Int,
                      periodType: PeriodType,
                      arrangementIndex: Int,
                      isOptional: Boolean,
                      groupId: Option[Long],
                      oldGroupId: Option[Long],
                      userId: Option[Long],
                      isDeleted: Boolean): Unit = execSyncInTransaction {

    certificateGoals
      .filterById(goalId)
      .map(goal => (goal.periodValue,
        goal.periodType,
        goal.arrangementIndex,
        goal.isOptional,
        goal.groupId,
        goal.oldGroupId,
        goal.modifiedDate,
        goal.userId,
        goal.isDeleted))
      .update((periodValue, periodType, arrangementIndex, isOptional, groupId, oldGroupId, now, userId, isDeleted))
  }

  override def modifyPeriod(goalId: Long, periodValue: Int, periodType: PeriodType): CertificateGoal = {
    execSyncInTransaction {
      val updateAction = certificateGoals
        .filterById(goalId)
        .map(goal => (goal.periodValue, goal.periodType))
        .update((periodValue, periodType))

      val resultAction = certificateGoals.filterById(goalId).result.head

      updateAction andThen resultAction
    }
  }

  override def modifyGroup(goalId: Long,
                           groupId: Option[Long],
                           oldGroupId: Option[Long],
                           isOptional: Boolean): CertificateGoal = execSyncInTransaction {

    val updateAction = certificateGoals
      .filterById(goalId)
      .map(goal => (goal.groupId, goal.oldGroupId, goal.isOptional, goal.modifiedDate))
      .update(groupId, oldGroupId, isOptional, now)

    val action = groupId match {
      case None => updateAction
      case Some(id) => updateAction andThen (certificateGoalGroups
        .filterById(id)
        .map(_.modifiedDate)
        .update(now))
    }

    val resultAction = certificateGoals.filterById(goalId).result.head

    action andThen resultAction
  }

  override def modifyDeletedState(goalId: Long, isDeleted: Boolean): Unit = execSync {
    certificateGoals
      .filterById(goalId)
      .map(_.isDeleted)
      .update(isDeleted)
  }

  override def updateIndexes(goals: Map[String, Int]): Unit = {
    if (goals.nonEmpty) {
      goals foreach { case (key, i) =>
        val id = key substring(key.lastIndexOf("_") + 1)
        if (key startsWith "goal") {
          execSync(certificateGoals.filterById(id.toLong).map(_.arrangementIndex).update(i))
        }
        if (key startsWith "group") {
          execSync(certificateGoalGroups.filter(_.id === id.toLong).map(_.arrangementIndex).update(i))
        }
      }
    }
  }
}