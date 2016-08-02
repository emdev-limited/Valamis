package com.arcusys.learn.liferay.update.version300.course

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CourseGradeTableComponent extends LongKeyTableComponent with TypeMapper {
  self: SlickProfile =>

  import driver.simple._

  case class CourseGrade(courseId: Long,
                         userId: Long,
                         grade: Option[Float],
                         date: DateTime,
                         comment: Option[String])

  class CourseGradeTable(tag: Tag) extends Table[CourseGrade](tag, tblName("COURSE_GRADE")) {
    val courseId = column[Long]("COURSE_ID")
    val userId = column[Long]("USER_ID")
    val grade = column[Option[Float]]("GRADE")
    val date = column[DateTime]("DATE")
    val comment = column[Option[String]]("COMMENT", O.Length(2000, varying = true))

    def * = (courseId, userId, grade, date, comment) <>(CourseGrade.tupled, CourseGrade.unapply)

    def pk = primaryKey(pkName("COURSE_GRADE"), (courseId, userId))
  }

  val courseGrades = TableQuery[CourseGradeTable]
}
