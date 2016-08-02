package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.lesson.scorm.model.manifest.RollupConsiderationType
import com.arcusys.valamis.lesson.scorm.model.manifest.RollupConsiderationType._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.RollupContributionModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait RollupContributionTableComponent extends LongKeyTableComponent
  with TypeMapper {
  self: SlickProfile with SequencingTableComponent =>

  import driver.simple._

  implicit val rollupConsiderationTypeMapper = enumerationMapper(RollupConsiderationType)


  class RollupContributionTable(tag: Tag) extends LongKeyTable[RollupContributionModel](tag, "SCO_ROLLUP_CONTRIBUTION") {

    def sequencingId = column[Long]("SEQUENCING_ID")

    def contributeToSatisfied = column[Option[RollupConsiderationType]]("CONTRIBUTE_TO_SATISFIED")

    def contributeToNotSatisfied = column[Option[RollupConsiderationType]]("CONTRIBUTE_TO_NOT_SATISFIED")

    def contributeToCompleted = column[Option[RollupConsiderationType]]("CONTRIBUTE_TO_COMPLETED")

    def contributeToIncompleted = column[Option[RollupConsiderationType]]("CONTRIBUTE_TO_INCOMPLETED")

    def objectiveMeasureWeight = column[BigDecimal]("OBJECTIVE_MEASURE_WEIGHT")

    def measureSatisfactionIfActive = column[Boolean]("MEASURE_SATISFACTION_IF_ACTIVE")

    def * = (id.?,
      sequencingId,
      contributeToSatisfied,
      contributeToNotSatisfied,
      contributeToCompleted,
      contributeToIncompleted,
      objectiveMeasureWeight,
      measureSatisfactionIfActive) <> (RollupContributionModel.tupled, RollupContributionModel.unapply)


    def update = (sequencingId,
      contributeToSatisfied,
      contributeToNotSatisfied,
      contributeToCompleted,
      contributeToIncompleted,
      objectiveMeasureWeight,
      measureSatisfactionIfActive) <>(tupleToEntity, entityToTuple)


    def sequencingFk = foreignKey(fkName("CONTRIBUTION_TO_SEQUENCING"), sequencingId, sequencingTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idxSequencingId = index("ROLLUP_CONTRIBUTION_SEQID", sequencingId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val rollupContributionTQ = TableQuery[RollupContributionTable]
}


