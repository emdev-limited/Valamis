package com.arcusys.valamis.gradebook.service.impl

import com.arcusys.valamis.gradebook.model.UserCourseResult
import com.arcusys.valamis.gradebook.service.{LessonGradeService, UserCourseResultService}
import com.arcusys.valamis.gradebook.storage.CourseTableComponent
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class UserCourseResultServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends UserCourseResultService
    with CourseTableComponent
    with SlickProfile {

  import driver.simple._

  def packageChecker: LessonGradeService
  def lessonService: LessonService

  private def get(courseId: Long, userId: Long): Option[UserCourseResult] = {
    db.withSession { implicit s =>
      completedCourses
        .filter(r => r.userId === userId && r.courseId === courseId)
        .firstOption
    }
  }

  private def isCourseCompleted(courseId: Long, userId: Long, lessonsCount: Long): Boolean = {
    get(courseId, userId).map(_.isCompleted) getOrElse {
      //TODO: synchronize
      val completedLessonCount = packageChecker.getCompletedLessonsCount(courseId, userId)
      val isCompleted = completedLessonCount == lessonsCount
      set(courseId, userId, isCompleted)

      isCompleted
    }
  }

  override def getCompletedCount(courseId: Long, userIds: Seq[Long]): Int = {
    val lessonsCount  = lessonService.getCount(courseId)

    if (lessonsCount <= 0) {
      0 // no lessons, then nobody complete course
    } else {
      userIds.count(isCourseCompleted(courseId, _, lessonsCount))
    }
  }

  override def set(courseId: Long, userId: Long, isCompleted: Boolean): Unit = {
    db.withTransaction { implicit s =>

      val updatedCount = completedCourses
        .filter(row => row.courseId === courseId && row.userId === userId)
        .map(_.isCompleted)
        .update(isCompleted)

      if (updatedCount == 0) {
        completedCourses += UserCourseResult(courseId, userId, isCompleted)
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
