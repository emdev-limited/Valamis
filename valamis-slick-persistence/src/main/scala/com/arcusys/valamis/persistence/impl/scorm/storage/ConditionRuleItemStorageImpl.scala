package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionRuleItem
import com.arcusys.valamis.lesson.scorm.storage.sequencing.ConditionRuleItemStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ConditionRuleItemModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ConditionRuleItemTableComponent, ConditionRuleTableComponent, RollupRuleTableComponent, SequencingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class ConditionRuleItemStorageImpl(val db: JdbcBackend#DatabaseDef,
                                   val driver: JdbcProfile)
  extends ConditionRuleItemStorage
  with ConditionRuleItemTableComponent
  with ConditionRuleTableComponent
  with RollupRuleTableComponent
  with SequencingTableComponent
  with SlickProfile {

  import driver.simple._

  override def createRollup(ruleId: Long, entity: ConditionRuleItem): Unit = db.withSession { implicit s =>
    conditionRuleItemTQ.insert(convertRollup(entity, ruleId))
  }

  override def deleteByRollup(ruleId: Long): Unit = db.withSession { implicit s =>
    conditionRuleItemTQ.filter(_.rollupRuleId === ruleId).delete
  }

  override def createCondition(ruleId: Long, entity: ConditionRuleItem): Unit = db.withSession { implicit s =>
    conditionRuleItemTQ.insert(convertCondition(entity, ruleId))
  }

  override def deleteByCondition(ruleId: Long): Unit = db.withSession { implicit s =>
    conditionRuleItemTQ.filter(_.conditionRuleId === ruleId).delete
  }

  override def getCondition(ruleId: Long): Seq[ConditionRuleItem] = db.withSession { implicit s =>
    conditionRuleItemTQ.filter(_.conditionRuleId === ruleId).list.map(convert)
  }

  override def getConditions(ruleIds: Seq[Long]): Map[Long, Seq[ConditionRuleItem]] = {
    ruleIds match {
      case Nil => Map()
      case ids: Seq[Long] =>
        db.withSession { implicit s =>
          conditionRuleItemTQ.filter(_.conditionRuleId inSet ids).list
            .groupBy(_.conditionRuleId.get)
            .mapValues(conditions => conditions.map(convert))
        }
    }
  }

  override def getRollup(ruleId: Long): Seq[ConditionRuleItem] = db.withSession { implicit s =>
    conditionRuleItemTQ.filter(_.rollupRuleId === ruleId).list.map(convert)
  }

  def convertRollup(entity: ConditionRuleItem, rollupRuleId: Long): ConditionRuleItemModel =
    ConditionRuleItemModel(
      None, entity.conditionType, entity.objectiveId, entity.measureThreshold, entity.inverse, Some(rollupRuleId), None
    )
  def convertCondition(entity: ConditionRuleItem, conditionRuleId: Long): ConditionRuleItemModel =
    ConditionRuleItemModel(
      None, entity.conditionType, entity.objectiveId, entity.measureThreshold, entity.inverse, None, Some(conditionRuleId)
    )

  def convert(entity: ConditionRuleItemModel): ConditionRuleItem = {
    new ConditionRuleItem(entity.conditionType, entity.objectiveId.filter(_.nonEmpty), entity.measureThreshold, entity.inverse)
  }
}
