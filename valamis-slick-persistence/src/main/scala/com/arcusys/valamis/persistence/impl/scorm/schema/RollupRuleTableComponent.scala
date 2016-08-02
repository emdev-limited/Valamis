package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.lesson.scorm.model.manifest.{ConditionCombination, RollupAction}
import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionCombination.ConditionCombination
import com.arcusys.valamis.lesson.scorm.model.manifest.RollupAction.RollupAction
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.RollupRuleModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait RollupRuleTableComponent extends LongKeyTableComponent
  with TypeMapper {
  self: SlickProfile with SequencingTableComponent =>

  import driver.simple._

  implicit val rollupActionMapper = enumerationMapper(RollupAction)
  implicit val condCombinationMapper = enumerationMapper(ConditionCombination)

  class RollupRuleTable(tag: Tag) extends LongKeyTable[RollupRuleModel](tag, "SCO_ROLLUP_RULE") {

    def sequencingId = column[Long]("SEQUENCING_ID")

    def combination = column[ConditionCombination]("COMBINATION")

    def childActivitySet = column[String]("CHILD_ACTIVITY_SET", O.DBType(varCharMax))

    def minimumCount = column[Option[Int]]("MINIMUM_COUNT")

    def minimumPercent = column[Option[BigDecimal]]("MINIMUM_PERCENT")

    def action = column[RollupAction]("ACTION")

    def * = (id.?,
      sequencingId,
      combination,
      childActivitySet,
      minimumCount,
      minimumPercent,
      action) <> (RollupRuleModel.tupled, RollupRuleModel.unapply)


    def update = (sequencingId,
      combination,
      childActivitySet,
      minimumCount,
      minimumPercent,
      action) <>(tupleToEntity, entityToTuple)

    def sequencingFk = foreignKey(fkName("ROLLUP_TO_SEQUENCING"), sequencingId, sequencingTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idxSeqId= index("ROLLUP_RULE_SEQID", sequencingId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val rollupRuleTQ = TableQuery[RollupRuleTable]
}