package com.arcusys.learn.liferay.update.version270.lesson

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait LessonGradeTableComponent extends TypeMapper {self: SlickProfile  =>

  import driver.simple._

  case class LessonGrade(lessonId: Long,
                         userId: Long,
                         grade: Option[Float],
                         date: DateTime,
                         comment: Option[String])

  class LessonGradeTable(tag: Tag) extends Table[LessonGrade](tag, tblName("LESSON_GRADE")) {
    val lessonId = column[Long]("LESSON_ID")
    val userId = column[Long]("USER_ID")
    val grade = column[Option[Float]]("GRADE")
    val date = column[DateTime]("DATE")
    val comment = column[Option[String]]("COMMENT", O.Length(2000, varying = true))

    def * = (lessonId, userId, grade, date, comment) <>(LessonGrade.tupled, LessonGrade.unapply)
  }

  val lessonGrades = TableQuery[LessonGradeTable]
}
