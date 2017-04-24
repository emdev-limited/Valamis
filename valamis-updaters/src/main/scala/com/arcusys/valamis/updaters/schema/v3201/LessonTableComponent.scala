package com.arcusys.valamis.updaters.schema.v3201

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent3, SlickProfile, TypeMapper}
import com.arcusys.valamis.updaters.schema.v3201.LessonType.LessonType
import org.joda.time.DateTime

/**
  * Created by mminin on 19.01.16.
  */
trait LessonTableComponent extends LongKeyTableComponent3 with TypeMapper { self: SlickProfile =>

  implicit val lessonTypeMapper = enumerationMapper(LessonType)

  import driver.api._

  class LessonTable(tag: Tag) extends Table[Lesson](tag, tblName("LESSON")) {
    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)

    def lessonType = column[LessonType]("LESSON_TYPE")
    def title = column[String]("TITLE", O.Length(2000, varying = true))
    def description = column[String]("DESCRIPTION", O.Length(2000, varying = true))
    def courseId = column[Long]("COURSE_ID")
    def logo = column[Option[String]]("LOGO", O.Length(254, varying = true))
    def isVisible = column[Option[Boolean]]("IS_VISIBLE")
    def beginDate = column[Option[DateTime]]("BEGIN_DATE")
    def endDate = column[Option[DateTime]]("END_DATE")
    def ownerId = column[Long]("OWNER_ID")
    def creationDate = column[DateTime]("CREATION_DATE")

    def * = (id, lessonType, title, description, logo, courseId, isVisible, beginDate, endDate, ownerId, creationDate) <> (Lesson.tupled, Lesson.unapply)
  }

  class PlayerInvisibleLessonTable(tag: Tag) extends Table[(Long, Long)](tag, tblName("LESSON_PLAYER_INVIS")) {
    def playerId = column[Long]("PLAYER_ID")
    def lessonId = column[Long]("LESSON_ID")


    def * = (playerId, lessonId)
    def pk = primaryKey(pkName("LESSON_TO_PLAYER_V"), (playerId, lessonId))

    def lesson = foreignKey(fkName("PLAYER_TO_LESSON_V"), lessonId, lessons)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  var lessons = TableQuery[LessonTable]
  var invisibleLessonViewers = TableQuery[PlayerInvisibleLessonTable]
}
