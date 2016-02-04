package com.arcusys.valamis.course

import com.arcusys.valamis.core.SlickProfile
import com.arcusys.valamis.course.model.UserCourseResult
import com.arcusys.valamis.course.storage.UserCourseResultStorage

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class UserCourseResultStorageImpl (val db: JdbcBackend#DatabaseDef,
                         val driver: JdbcProfile) 
  extends UserCourseResultStorage
  with CourseTableComponent
  with SlickProfile {

  import driver.simple._

  def get(courseId: Long, userId: Long): Option[UserCourseResult] = db.withSession { implicit session =>
    completedCourses
      .filter(r => r.userId === userId && r.courseId === courseId)
      .firstOption
  }

  def getByUserId(userId: Long): Seq[UserCourseResult] = db.withSession { implicit session =>
    completedCourses
      .filter(r => r.userId === userId)
      .run
  }

  def insertOrUpdate(userCourse: UserCourseResult) {
    db.withSession { implicit session =>
      //Slick doesn't support insertOrUpdate with composite PK key, so use "update else insert" logic

      val updatedCount = completedCourses
        .filter(row => row.courseId === userCourse.courseId && row.userId === userCourse.userId)
        .map(_.isCompleted)
        .update(userCourse.isCompleted)

      if (updatedCount == 0)
        completedCourses.insert(userCourse)
    }
  }

  def update(courseId: Long, isCompleted: Boolean) = db.withSession { implicit session =>
    completedCourses
      .filter(_.courseId === courseId)
      .map(_.isCompleted)
      .update(isCompleted)
  }

  def delete(courseId: Long) = db.withSession { implicit session =>
    completedCourses
      .filter(_.courseId === courseId)
      .delete
  }
}
