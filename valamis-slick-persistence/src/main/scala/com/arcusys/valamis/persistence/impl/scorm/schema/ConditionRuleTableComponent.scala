package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.impl.scorm.model.ConditionRuleModel
import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionCombination.ConditionCombination
import com.arcusys.valamis.lesson.scorm.model.manifest.{ConditionCombination, ConditionRuleType}
import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionRuleType._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait ConditionRuleTableComponent extends LongKeyTableComponent
  with TypeMapper {
  self: SlickProfile with SequencingTableComponent =>

  import driver.simple._

  implicit val conditionRuleTypeMapper = enumerationMapper(ConditionRuleType)
  implicit val conditionCombinationMapper = enumerationMapper(ConditionCombination)


  class ConditionRuleTable(tag: Tag) extends LongKeyTable[ConditionRuleModel](tag, "SCO_CONDITION_RULE") {

    def sequencingId = column[Long]("SEQUENCING_ID")

    def combination = column[ConditionCombination]("COMBINATION")

    def ruleType = column[ConditionRuleType]("RULE_TYPE")

    def action = column[Option[String]]("ACTION")


    def * = (id.?,
      sequencingId,
      combination,
      ruleType,
      action) <> (ConditionRuleModel.tupled, ConditionRuleModel.unapply)


    def update = (sequencingId,
      combination,
      ruleType,
      action) <> (tupleToEntity, entityToTuple)

    def sequencingFk = foreignKey(fkName("CONDITION_TO_SEQUENCING"), sequencingId, sequencingTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idxSequencingIdRuleType = index("CONDITION_SEQID_RULETYPE", (sequencingId, ruleType))

    def idxSequencingId = index("CONDITION_SEQID", sequencingId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val conditionRuleTQ = TableQuery[ConditionRuleTable]
}

