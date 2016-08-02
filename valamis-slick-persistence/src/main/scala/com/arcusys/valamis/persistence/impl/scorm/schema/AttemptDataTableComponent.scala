package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.AttemptDataModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple
import slick.driver.MySQLDriver

trait AttemptDataTableComponent extends LongKeyTableComponent with TypeMapper {
  self: SlickProfile
    with AttemptTableComponent =>

  import driver.simple._

  class AttemptDataTable(tag: Tag) extends LongKeyTable[AttemptDataModel](tag, "SCO_ATTEMPT_DATA") {

    def dataKey = column[String]("DATA_KEY", O.Length(3000, true))

    def dataValue = column[Option[String]]("DATA_VALUE", O.DBType(varCharMax))

    def attemptId = column[Long]("ATTEMPT_ID")

    def activityId = column[String]("ACTIVITY_ID", O.Length(512, true))

    def * = (id.?,
      dataKey,
      dataValue,
      attemptId,
      activityId) <> (AttemptDataModel.tupled, AttemptDataModel.unapply)

    def update = (dataKey,
      dataValue,
      attemptId,
      activityId) <>(tupleToEntity, entityToTuple)

    def attemptFk = foreignKey(fkName("ATTEMPT_TO_ATTEMPTDATA"), attemptId, attemptTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    if (!slickDriver.isInstanceOf[MySQLDriver]) {
      def idxAttemptIdActivityId = index("ATTEMPT_DATA_ATTID_ACTID", (attemptId, activityId))
    }

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val attemptDataTQ = TableQuery[AttemptDataTable]
}


