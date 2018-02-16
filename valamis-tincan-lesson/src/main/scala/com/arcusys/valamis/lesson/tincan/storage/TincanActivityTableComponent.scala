package com.arcusys.valamis.lesson.tincan.storage

import com.arcusys.valamis.lesson.tincan.model.TincanActivity
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

/**
  * Created by mminin on 19.01.16.
  */
trait TincanActivityTableComponent extends LongKeyTableComponent with TypeMapper {self:SlickProfile =>

  import driver.simple._

  class TincanActivityTable(tag: Tag) extends LongKeyTable[TincanActivity](tag, "TINCAN_ACTIVITY") {
    def lessonId = column[Long]("LESSON_ID")
    def activityId = column[String]("ACTIVITY_ID", O.Length(2000, true))
    def activityType = column[String]("ACTIVITY_TYPE", O.Length(255, true))
    def name = column[String]("NAME", O.Length(2000, true))
    def description = column[String]("DESCRIPTION", O.Length(2000, true))
    def launch = column[Option[String]]("LAUNCH", O.Length(500, true))
    def resource = column[Option[String]]("RESOURCE", O.Length(500, true))

    def * = (lessonId, activityId, activityType, name, description, launch, resource, id.?) <> (TincanActivity.tupled, TincanActivity.unapply)

    def update = (lessonId, activityId, activityType, name, description, launch, resource) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val tincanActivitiesTQ = TableQuery[TincanActivityTable]
}
