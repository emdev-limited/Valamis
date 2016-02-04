package com.arcusys.valamis.course

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.course.model.UserCourseResult

import scala.slick.driver.JdbcProfile

trait CourseTableComponent {
  protected val driver: JdbcProfile
  import driver.simple._

  class CompletedCourseTable(tag : Tag) extends Table[UserCourseResult](tag, tblName("COMPLETED_COURSE")) {
    def courseId = column[Long]("COURSE_ID")
    def userId = column[Long]("USER_ID")
    def isCompleted = column[Boolean]("IS_COMPLETED")

    def * = (courseId, userId, isCompleted) <> (UserCourseResult.tupled, UserCourseResult.unapply)

    def pk = primaryKey("COMPLETED_COURSE_pk", (courseId, userId))
  }

  val completedCourses = TableQuery[CompletedCourseTable]
}
