package com.arcusys.valamis.updaters.version320.schema3208

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CourseInstructorTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.api._

  case class CourseInstructor(courseId: Long,
                              instructorId: Long,
                              modifiedDate: DateTime)

  class CourseInstructorTable(tag: Tag) extends Table[CourseInstructor](tag, tblName("COURSE_INSTRUCTOR")) {

    def courseId = column[Long]("COURSE_ID")

    def instructorId = column[Long]("INSTRUCTOR_ID")

    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def * = (courseId, instructorId, modifiedDate) <> (CourseInstructor.tupled, CourseInstructor.unapply)

    def pk = primaryKey(pkName("COURSE_TO_INSTRUCTOR"), (courseId, instructorId))
  }

  val courseInstructors = TableQuery[CourseInstructorTable]
}