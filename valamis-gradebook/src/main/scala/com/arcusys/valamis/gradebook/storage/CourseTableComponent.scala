package com.arcusys.valamis.gradebook.storage

import com.arcusys.valamis.gradebook.model.UserCourseResult
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}


trait CourseTableComponent extends TypeMapper { self: SlickProfile =>

  import driver.simple._

  //TODO: check, combine with COURSE_GRADE or remove ?
  class CompletedCourseTable(tag : Tag) extends Table[UserCourseResult](tag, tblName("COMPLETED_COURSE")) {
    val courseId = column[Long]("COURSE_ID")
    val userId = column[Long]("USER_ID")
    val isCompleted = column[Boolean]("IS_COMPLETED")

    def * = (courseId, userId, isCompleted) <> (UserCourseResult.tupled, UserCourseResult.unapply)

    def pk = primaryKey("COMPLETED_COURSE_pk", (courseId, userId))
  }

  val completedCourses = TableQuery[CompletedCourseTable]
}
