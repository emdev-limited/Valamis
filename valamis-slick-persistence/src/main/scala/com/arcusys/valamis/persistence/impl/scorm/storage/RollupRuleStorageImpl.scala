package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.{ChildActivitySetAtLeastPercent, _}
import com.arcusys.valamis.lesson.scorm.storage.sequencing.{ConditionRuleItemStorage, RollupRuleStorage}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.RollupRuleModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{RollupRuleTableComponent, SequencingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


abstract class RollupRuleStorageImpl(val db: JdbcBackend#DatabaseDef,
                                     val driver: JdbcProfile)
  extends RollupRuleStorage
    with RollupRuleTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def conditionRuleItemStorage: ConditionRuleItemStorage

  override def create(sequencingId: Long, entity: RollupRule): Unit = {

    db.withSession { implicit s =>
      val ruleId = rollupRuleTQ.returning(rollupRuleTQ.map(_.id)).insert(entity.convert(sequencingId))
      entity.conditions.conditions.foreach(conditionRuleItemStorage.createRollup(ruleId, _))
    }
  }

  override def deleteBySequencing(sequencingId: Long): Unit = db.withSession { implicit s =>
    rollupRuleTQ.filter(_.sequencingId === sequencingId).map(_.id).list.foreach(conditionRuleItemStorage.deleteByRollup)
    rollupRuleTQ.filter(_.sequencingId === sequencingId).delete
  }

  override def get(sequencingId: Long): Seq[RollupRule] = db.withSession { implicit s =>
    rollupRuleTQ.filter(_.sequencingId === sequencingId).list
      .map(rr => {
        val conditions = conditionRuleItemStorage.getRollup(rr.id.get)
        rr.convert(conditions)
      })
  }

  implicit class RollupRuleToRow(entity: RollupRule) {
    def convert(sequencingId: Long): RollupRuleModel = {
      val (childActivitySet, minimumCount, minimumPercent) = entity.childActivitySet match {
        case ChildActivitySetAll => ("all", None, None)
        case ChildActivitySetAny => ("any", None, None)
        case ChildActivitySetNone => ("none", None, None)
        case ChildActivitySetAtLeastCount(count) => ("atLeastCount", Some(count), None)
        case ChildActivitySetAtLeastPercent(percent) => ("atLeastPercent", None, Some(percent))
      }
      RollupRuleModel(
        None, sequencingId, entity.conditions.combination, childActivitySet, minimumCount, minimumPercent, entity.action
      )
    }

  }

  implicit class RollupRuleToModel(entity: RollupRuleModel) {
    def convert(conditions: Seq[ConditionRuleItem]): RollupRule = {
      val conditionSet = new RuleConditionSet(conditions, entity.combination)

      new RollupRule(ChildActivitySet.parse(entity.childActivitySet, entity.minimumCount, entity.minimumPercent), conditionSet, entity.action)
    }
  }

}
