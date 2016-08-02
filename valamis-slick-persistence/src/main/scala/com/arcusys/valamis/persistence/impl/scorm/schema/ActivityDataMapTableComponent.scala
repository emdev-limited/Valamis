package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityDataMapModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple
import slick.driver.MySQLDriver

trait ActivityDataMapTableComponent extends LongKeyTableComponent with TypeMapper
 with ActivityTableComponent{
  self: SlickProfile =>

  import driver.simple._

  class ActivityDataMapTable(tag: Tag) extends LongKeyTable[ActivityDataMapModel](tag, "SCO_ACTIVITY_DATA_MAP") {

    def packageId = column[Option[Long]]("PACKAGE_ID")

    def activityId = column[Option[String]]("ACTIVITY_ID", O.Length(512, true))

    def targetId = column[String]("TARGET_ID", O.DBType(varCharMax))

    def readSharedData = column[Boolean]("READ_SHARED_DATA")

    def writeSharedData = column[Boolean]("WRITE_SHARED_DATA")

    def * = (id.?,
      packageId,
      activityId,
      targetId,
      readSharedData,
      writeSharedData) <>(ActivityDataMapModel.tupled, ActivityDataMapModel.unapply)

    def update = (packageId, activityId, targetId, readSharedData, writeSharedData) <>(tupleToEntity, entityToTuple)

    if (!slickDriver.isInstanceOf[MySQLDriver]) {
      def idxParentIdActivityId = index("ACT_DATA_MAP_PACKID_ACTID", (packageId, activityId))
    }

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val activityDataMapTQ = TableQuery[ActivityDataMapTable]
}


