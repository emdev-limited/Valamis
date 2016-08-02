package com.arcusys.valamis.lesson.storage

import com.arcusys.valamis.lesson.model.LessonGrade
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait LessonGradeTableComponent
  extends LongKeyTableComponent
    with TypeMapper
    with LessonTableComponent {
  self: SlickProfile  =>

  import driver.simple._

  class LessonGradeTable(tag: Tag) extends Table[LessonGrade](tag, tblName("LESSON_GRADE")) {
    val lessonId = column[Long]("LESSON_ID")
    val userId = column[Long]("USER_ID")
    val grade = column[Option[Float]]("GRADE")
    val date = column[DateTime]("DATE")
    val comment = column[Option[String]]("COMMENT", O.Length(2000, varying = true))

    def * = (lessonId, userId, grade, date, comment) <>(LessonGrade.tupled, LessonGrade.unapply)

    def pk = primaryKey(pkName("LESSON_GRADE"), (lessonId, userId))
    def lesson = foreignKey(fkName("LESSON_GRADE_TO_LESSON"), lessonId, lessons)(_.id)
  }

  val lessonGrades = TableQuery[LessonGradeTable]
}
