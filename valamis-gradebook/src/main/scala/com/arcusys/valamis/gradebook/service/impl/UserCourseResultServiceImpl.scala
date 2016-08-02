package com.arcusys.valamis.gradebook.service.impl

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.gradebook.model.UserCourseResult
import com.arcusys.valamis.gradebook.service.{LessonGradeService, UserCourseResultService}
import com.arcusys.valamis.gradebook.storage.CourseTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class UserCourseResultServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends UserCourseResultService
    with CourseTableComponent
    with SlickProfile {

  import driver.simple._

  def packageChecker: LessonGradeService

  def get(courseId: Long, userId: Long): Option[UserCourseResult] = db.withSession { implicit s =>
    completedCourses
      .filter(r => r.userId === userId && r.courseId === courseId)
      .firstOption
  }

  override def isCompleted(courseId: Long, user: LUser, packagesCount: Option[Long]): Boolean = {
    get(courseId, user.getUserId).map(_.isCompleted) match {
      case Some(isCompleted) => isCompleted
      case None =>
        val isCompleted = packageChecker.isCourseCompleted(courseId, user.getUserId)

        set(courseId, user.getUserId, isCompleted)

        isCompleted
    }
  }

  override def set(courseId: Long, userId: Long, isCompleted: Boolean): Unit = {
    db.withTransaction { implicit s =>

      val updatedCount = completedCourses
        .filter(row => row.courseId === courseId && row.userId === userId)
        .map(_.isCompleted)
        .update(isCompleted)

      if (updatedCount == 0) {
        completedCourses += new UserCourseResult(courseId, userId, isCompleted)
      }
    }
  }

  override def setCourseNotCompleted(courseId: Long) {
    db.withTransaction { implicit s =>
      completedCourses
        .filter(_.courseId === courseId)
        .map(_.isCompleted)
        .update(false)
    }
  }

  override def resetCourseResults(courseId: Long) {
    db.withTransaction { implicit s =>
      completedCourses
        .filter(_.courseId === courseId)
        .delete
    }
  }
}
