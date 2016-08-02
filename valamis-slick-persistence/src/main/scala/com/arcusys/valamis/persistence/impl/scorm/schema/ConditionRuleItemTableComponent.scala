package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionType
import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionType._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ConditionRuleItemModel
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple

trait ConditionRuleItemTableComponent extends LongKeyTableComponent
  with TypeMapper { self: SlickProfile
    with ConditionRuleTableComponent
    with RollupRuleTableComponent =>

  implicit val conditionTypeMapper = enumerationMapper(ConditionType)

  import driver.simple._
  class ConditionRuleItemTable(tag: Tag) extends LongKeyTable[ConditionRuleItemModel](tag, "SCO_CONDITION_RULE_ITEM") {

    def conditionType = column[ConditionType]("CONDITION_TYPE")

    def objectiveId = column[Option[String]]("OBJECTIVE_ID", O.Length(3000, true))

    def measureThreshold = column[Option[BigDecimal]]("MEASURE_THRESHOLD")

    def inverse = column[Boolean]("INVERSE")

    def rollupRuleId = column[Option[Long]]("ROLLUP_RULE_ID")

    def conditionRuleId = column[Option[Long]]("CONDITION_RULE_ID")


    def * = (id.?,
      conditionType,
      objectiveId,
      measureThreshold,
      inverse,
      rollupRuleId,
      conditionRuleId) <>(ConditionRuleItemModel.tupled, ConditionRuleItemModel.unapply)


    def update = (conditionType,
      objectiveId,
      measureThreshold,
      inverse,
      rollupRuleId,
      conditionRuleId) <>(tupleToEntity, entityToTuple)

    def conditionFk = foreignKey(fkName("CONDITION_TO_RULE"), conditionRuleId, conditionRuleTQ)(_.id, onDelete = ForeignKeyAction.NoAction)
    def rollupFk = foreignKey(fkName("ROLLUP_TO_RULE"), rollupRuleId, rollupRuleTQ)(_.id, onDelete = ForeignKeyAction.NoAction)


    def idxRollup = index("RULE_CONDITION_ROLLRULEID", rollupRuleId)

    def idxCondition = index("RULE_CONDITION_CONDRULEID", conditionRuleId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }
  val conditionRuleItemTQ = TableQuery[ConditionRuleItemTable]
}


