package com.arcusys.learn.liferay.update.version300.lesson

import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait LessonTableComponent extends TypeMapper { self: SlickProfile =>

  implicit val lessonTypeMapper = enumerationMapper(LessonType)
  implicit val periodTypeMapper = enumerationMapper(PeriodTypes)
  implicit val lessonViewerTypeMapper = enumerationIdMapper(MemberTypes)

  import driver.simple._

  case class Lesson(id: Long,
                    lessonType: LessonType,
                    title: String,
                    description: String,
                    logo: Option[String],
                    courseId: Long,
                    isVisible: Option[Boolean],
                    beginDate: Option[DateTime],
                    endDate: Option[DateTime],
                    ownerId: Long,
                    creationDate: DateTime,
                    requiredReview: Boolean,
                    scoreLimit: Double)

  class LessonTable(tag: Tag) extends Table[Lesson](tag, tblName("LESSON")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def lessonType = column[LessonType]("LESSON_TYPE")
    def title = column[String]("TITLE", O.Length(2000, varying = true))
    def description = column[String]("DESCRIPTION", O.Length(2000, varying = true))
    def courseId = column[Long]("COURSE_ID")
    def logo = column[Option[String]]("LOGO", O.Length(254, true))
    def isVisible = column[Option[Boolean]]("IS_VISIBLE")
    def beginDate = column[Option[DateTime]]("BEGIN_DATE")
    def endDate = column[Option[DateTime]]("END_DATE")
    def ownerId = column[Long]("OWNER_ID")
    def creationDate = column[DateTime]("CREATION_DATE")
    def requiredReview = column[Boolean]("REQUIRED_REVIEW")
    def scoreLimit = column[Double]("SCORE_LIMIT")

    def * = (
      id,
      lessonType,
      title,
      description,
      logo,
      courseId,
      isVisible,
      beginDate,
      endDate,
      ownerId,
      creationDate,
      requiredReview,
      scoreLimit) <> (Lesson.tupled, Lesson.unapply)

  }

  var lessons = TableQuery[LessonTable]
}
