package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.impl.scorm.model.SequencingModel
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._
import slick.driver.MySQLDriver

trait SequencingTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>

  import driver.simple._

  class SequencingTable(tag: Tag) extends LongKeyTable[SequencingModel](tag, "SCO_SEQUENCING") {

    def packageId = column[Option[Long]]("PACKAGE_ID")

    def activityId = column[Option[String]]("ACTIVITY_ID", O.Length(512, true))

    def sharedId = column[Option[String]]("SHARED_ID", O.DBType(varCharMax))

    def sharedSequencingIdReference = column[Option[String]]("SHARED_SEQ_ID_REFERENCE", O.DBType(varCharMax))

    def cAttemptObjectiveProgressChild = column[Boolean]("C_ATTEMPT_OBJ_PROGRESS_CHILD")

    def cAttemptAttemptProgressChild = column[Boolean]("C_ATTEMPT_ATT_AROGRESS_CHILD")

    def attemptLimit = column[Option[Int]]("ATTEMPT_LIMIT")

    def durationLimitInMilliseconds = column[Option[Long]]("DURATION_LIMIT_IN_MILLESEC")

    def preventChildrenActivation = column[Boolean]("PREVENT_CHILDREN_ACTIVATION")

    def constrainChoice = column[Boolean]("CONSTRAIN_CHOICE")


    def * = (id.?,
      packageId,
      activityId,
      sharedId,
      sharedSequencingIdReference,
      cAttemptObjectiveProgressChild,
      cAttemptAttemptProgressChild,
      attemptLimit,
      durationLimitInMilliseconds,
      preventChildrenActivation,
      constrainChoice) <> (SequencingModel.tupled, SequencingModel.unapply)

    def update = (packageId,
      activityId,
      sharedId,
      sharedSequencingIdReference,
      cAttemptObjectiveProgressChild,
      cAttemptAttemptProgressChild,
      attemptLimit,
      durationLimitInMilliseconds,
      preventChildrenActivation,
      constrainChoice) <> (tupleToEntity, entityToTuple)

    if (!slickDriver.isInstanceOf[MySQLDriver]) {
      def idx = index("SEQUENCING_ACTID_PACKAGEID", (activityId, packageId))
    }
    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val sequencingTQ = TableQuery[SequencingTable]
}