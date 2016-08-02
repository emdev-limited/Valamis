package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{CertificateGoal, GoalType}
import com.arcusys.valamis.certificate.storage.CertificateGoalRepository
import com.arcusys.valamis.certificate.storage.schema.{CertificateGoalGroupTableComponent, CertificateGoalTableComponent}
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class CertificateGoalRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateGoalRepository
  with CertificateGoalTableComponent
  with CertificateGoalGroupTableComponent
  with SlickProfile {

  import driver.simple._

  override def getBy(certificateId: Long): Seq[CertificateGoal] =
    db.withTransaction { implicit s =>
      certificateGoals.filter(x => x.certificateId === certificateId)
        .list
    }

  override def getById(id: Long): CertificateGoal =
    db.withTransaction { implicit s =>
      certificateGoals.filter(_.id === id)
        .first
    }

  override def getByCertificate(certificateId: Long): Seq[CertificateGoal] =
    db.withTransaction { implicit s =>
      certificateGoals.filter(_.certificateId === certificateId)
        .list
    }

  override def getIdsByGroup(groupId: Long): Seq[Long] =
    db.withTransaction { implicit s =>
      certificateGoals
        .filter(_.groupId === groupId)
        .map(_.id)
        .list
    }

  def deleteByGroup(groupId: Long): Unit =
    db.withTransaction { implicit s =>
      certificateGoals
        .filter(_.groupId === groupId)
        .delete
    }

  override def delete(id: Long): Unit =
    db.withTransaction { implicit s =>
      certificateGoals.filter(_.id === id).delete
    }

  override def create(certificateId: Long,
                      goalType: GoalType.Value,
                      periodValue: Int,
                      periodType: PeriodType,
                      arrangementIndex: Int,
                      isOptional: Boolean,
                      groupId: Option[Long] = None): Long = {
    db.withTransaction { implicit s =>
      certificateGoals
        .map(g => (g.certificateId, g.goalType, g.periodValue, g.periodType, g.arrangementIndex, g.isOptional, g.groupId))
        .returning(certificateGoals.map(_.id))
        .insert(certificateId, goalType, periodValue, periodType, arrangementIndex, isOptional, groupId)
    }
  }

  override def modify(goalId: Long,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             isOptional: Boolean,
             groupId: Option[Long]): CertificateGoal = {
    db.withTransaction { implicit s =>
      certificateGoals
        .filter(_.id === goalId)
        .map(goal => (goal.periodValue, goal.periodType, goal.arrangementIndex, goal.isOptional, goal.groupId))
        .update((periodValue, periodType, arrangementIndex, isOptional, groupId))

      certificateGoals.filter(_.id === goalId).first
    }
  }

  override def modifyPeriod(goalId: Long, periodValue: Int, periodType: PeriodType): CertificateGoal = {
    db.withTransaction { implicit s =>
      certificateGoals
        .filter(_.id === goalId)
        .map(goal => (goal.periodValue, goal.periodType))
        .update((periodValue, periodType))

      certificateGoals.filter(_.id === goalId).first
    }
  }

  def modifyGroup(goalId: Long, groupId: Option[Long], isOptional: Boolean): CertificateGoal = {
    db.withTransaction { implicit s =>
      certificateGoals
        .filter(_.id === goalId)
        .map(goal =>(goal.groupId, goal.isOptional))
        .update(groupId, isOptional)

      certificateGoals.filter(_.id === goalId).first
    }
  }

  override def updateIndexes(goals: Map[String, Int]): Unit = {
    if (goals.nonEmpty) db.withTransaction { implicit s =>
      goals foreach { case (key, i) =>
        val id = key substring(key.indexOf("_") + 1)
          if (key startsWith "goal") {
          certificateGoals.filter(_.id === id.toLong).map(_.arrangementIndex).update(i)
        }
        if (key startsWith "group") {
            certificateGoalGroups.filter(_.id === id.toLong).map(_.arrangementIndex).update(i)
          }
      }
    }
  }
}