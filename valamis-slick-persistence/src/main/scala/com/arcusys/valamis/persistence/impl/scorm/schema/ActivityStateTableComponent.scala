package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple
import slick.driver.MySQLDriver

trait ActivityStateTableComponent extends LongKeyTableComponent with TypeMapper
with ActivityTableComponent {
  self: SlickProfile
    with ActivityStateNodeTableComponent
    with ActivityStateTreeTableComponent =>

  import driver.simple._

  class ActivityStateTable(tag: Tag) extends LongKeyTable[ActivityStateModel](tag, "SCO_ACTIVITY_STATE") {

    def packageId = column[Option[Long]]("PACKAGE_ID")

    def activityId = column[String]("ACTIVITY_ID", O.Length(512, true))

    def active = column[Boolean]("ACTIVITY")

    def suspended = column[Boolean]("SUSPENDED")

    def attemptCompleted = column[Option[Boolean]]("ATTEMPT_COMPLETED")

    def attemptCompletionAmount = column[Option[BigDecimal]]("ATTEMPT_COMPLETION_AMOUNT")

    def attemptAbsoluteDuration = column[BigDecimal]("ATTEMPT_ABSOLUTE_DURATION")

    def attemptExperiencedDuration = column[BigDecimal]("ATTEMPT_EXPERIENCED_DURATION")

    def activityAbsoluteDuration = column[BigDecimal]("ACTIVITY_ABSOLUTE_DURATION")

    def activityExperiencedDuration = column[BigDecimal]("ACTIVITY_EXPERIENCED_DURATION")

    def attemptcount = column[Int]("ATTEMPT_COUNT")

    def activityStateNodeId = column[Option[Long]]("ACTIVITY_STATE_NODE_ID")

    def activityStateTreeId = column[Option[Long]]("ACTIVITY_STATE_TREE_ID")

    def * = (id.?,
      packageId,
      activityId,
      active,
      suspended,
      attemptCompleted,
      attemptCompletionAmount,
      attemptAbsoluteDuration,
      attemptExperiencedDuration,
      activityAbsoluteDuration,
      activityExperiencedDuration,
      attemptcount,
      activityStateNodeId,
      activityStateTreeId) <>(ActivityStateModel.tupled, ActivityStateModel.unapply)


    def update = (packageId,
      activityId,
      active,
      suspended,
      attemptCompleted,
      attemptCompletionAmount,
      attemptAbsoluteDuration,
      attemptExperiencedDuration,
      activityAbsoluteDuration,
      activityExperiencedDuration,
      attemptcount,
      activityStateNodeId,
      activityStateTreeId) <>(tupleToEntity, entityToTuple)

    if (!slickDriver.isInstanceOf[MySQLDriver]) {
       def idxNodeIdActivityId = index("ACT_DATA_NODEID_ACTID", (activityStateNodeId, activityId))
       def idxNodeIdTreeId = index("ACT_DATA_NODEID_TREEID", (activityId, activityStateNodeId, activityStateTreeId))
    }

    def idxNodeId = index("ACT_DATA_NODEID", activityStateNodeId)

    def activityStateFK = foreignKey(fkName("ACTIVITYSTATE_TONODEID"), activityStateNodeId, activityStateNodeTQ)(_.id)

    def activityTreeFK = foreignKey(fkName("ACTIVITYTREE_TONODEID"), activityStateTreeId, activityStateTreeTQ)(_.id)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val activityStateTQ = TableQuery[ActivityStateTable]
}