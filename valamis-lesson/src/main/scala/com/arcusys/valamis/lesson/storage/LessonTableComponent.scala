package com.arcusys.valamis.lesson.storage

import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import org.joda.time.DateTime
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

/**
  * Created by mminin on 19.01.16.
  */
trait LessonTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>

  implicit val lessonTypeMapper = enumerationMapper(LessonType)
  implicit val periodTypeMapper = enumerationMapper(PeriodTypes)
  implicit val lessonViewerTypeMapper = enumerationIdMapper(MemberTypes)

  import driver.simple._

  class LessonTable(tag: Tag) extends LongKeyTable[Lesson](tag, "LESSON") {

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

    def update = (
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
      scoreLimit) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  class LessonLimitTable(tag: Tag) extends Table[LessonLimit](tag, tblName("LESSON_LIMIT")) {
    def lessonId = column[Long]("LESSON_ID", O.PrimaryKey)
    def passingLimit = column[Option[Int]]("PASSING_LIMIT")
    def rerunInterval = column[Option[Int]]("RERUN_INTERVAL")
    def rerunIntervalType = column[PeriodType]("RERUN_INTERVAL_TYPE")

    def * = (lessonId, passingLimit, rerunInterval, rerunIntervalType) <> (LessonLimit.tupled, LessonLimit.unapply)

    def lesson = foreignKey(fkName("LIMIT_TO_LESSON"), lessonId, lessons)(_.id)
  }

  class PlayerLessonTable(tag: Tag) extends Table[PlayerLesson](tag, tblName("LESSON_TO_PLAYER")) {
    def playerId = column[Long]("PLAYER_ID")
    def lessonId = column[Long]("LESSON_ID")
    def index = column[Int]("INDEX")

    def * = (playerId, lessonId, index) <> (PlayerLesson.tupled, PlayerLesson.unapply)
    def pk = primaryKey(pkName("LESSON_TO_PLAYER"), (playerId, lessonId))

    def lesson = foreignKey(fkName("PLAYER_TO_LESSON"), lessonId, lessons)(_.id)
  }

  class LessonViewerTable(tag: Tag) extends Table[LessonViewer](tag, tblName("LESSON_VIEWER")) {
    def lessonId = column[Long]("LESSON_ID")
    def viewerId = column[Long]("VIEWER_ID")
    def viewerType = column[MemberTypes.Value]("VIEWER_TYPE")

    def * = (lessonId, viewerId, viewerType) <> (LessonViewer.tupled, LessonViewer.unapply)
    def pk = primaryKey(pkName("LESSON_TO_USER"), (lessonId, viewerId, viewerType))

    def lesson = foreignKey(fkName("USER_ID_TO_LESSON"), lessonId, lessons)(_.id)
  }

  val lessons = TableQuery[LessonTable]
  val lessonLimits = TableQuery[LessonLimitTable]
  val playerLessons = TableQuery[PlayerLessonTable]
  val lessonViewers = TableQuery[LessonViewerTable]
}
