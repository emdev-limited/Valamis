package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.schema.CourseGoalTableComponent
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, CourseGoalStorage}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class CourseGoalStorageImpl (val db: JdbcBackend#DatabaseDef,
                             val driver: JdbcProfile)
  extends CourseGoalStorage
  with CourseGoalTableComponent
  with SlickProfile {

  import driver.simple._

  def certificateGoalRepository: CertificateGoalRepository

  override def get(certificateId: Long, courseId: Long) = db.withSession { implicit session =>
    courseGoals.filter(ag => ag.certificateId === certificateId && ag.courseId === courseId).firstOption
  }

  override def getBy(goalId: Long): Option[CourseGoal] = db.withTransaction { implicit session =>
    courseGoals.filter(_.goalId === goalId).firstOption
  }

  override def create(certificateId: Long,
                      courseId: Long,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): CourseGoal =
    db.withTransaction { implicit session =>
    val goalId = certificateGoalRepository.create(
        certificateId,
        GoalType.Course,
        periodValue,
        periodType,
        arrangementIndex,
        isOptional,
        groupId)

    val courseGoal = CourseGoal(
      goalId,
      certificateId,
      courseId)
    courseGoals insert courseGoal
    courseGoals.filter(ag => ag.certificateId === certificateId && ag.courseId === courseId).first
  }

  override def getByCertificateId(certificateId: Long): Seq[CourseGoal] = db.withSession { implicit session =>
    courseGoals
      .filter(_.certificateId === certificateId)
      .run
  }
}