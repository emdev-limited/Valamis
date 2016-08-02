package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateTreeModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple

trait ActivityStateTreeTableComponent extends LongKeyTableComponent with TypeMapper {
  self: SlickProfile
    with AttemptTableComponent =>

  import driver.simple._

  class ActivityStateTreeTable(tag: Tag) extends LongKeyTable[ActivityStateTreeModel](tag, "SCO_ACTIVITY_STATE_TREE") {

    def currentActivityId = column[Option[String]]("CURRENT_ACTIVITY_ID", O.DBType(varCharMax))

    def suspendedActivityId = column[Option[String]]("SUSPENDED_ACTIVITY_ID", O.DBType(varCharMax))

    def attemptId = column[Option[Long]]("ATTEMPT_ID")

    def * = (id.?,
      currentActivityId,
      suspendedActivityId,
      attemptId) <> (ActivityStateTreeModel.tupled, ActivityStateTreeModel.unapply)


    def update = (currentActivityId, suspendedActivityId, attemptId) <>(tupleToEntity, entityToTuple)

    def attemptFk = foreignKey(fkName("ATTEMPT_TO_ACTIVITYSTATE"), attemptId, attemptTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idxAttemptId = index("ACT_STATE_TREE_ATTEMPID", attemptId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val activityStateTreeTQ= TableQuery[ActivityStateTreeTable]
}
