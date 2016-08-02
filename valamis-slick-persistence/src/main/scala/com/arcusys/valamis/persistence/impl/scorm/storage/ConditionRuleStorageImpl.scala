package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest._
import com.arcusys.valamis.lesson.scorm.storage.sequencing.{ConditionRuleItemStorage, ConditionRuleStorage}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ConditionRuleModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ConditionRuleTableComponent, SequencingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class ExitConditionRuleStorageImpl(val db: JdbcBackend#DatabaseDef,
                                            val driver: JdbcProfile)
  extends ConditionRuleStorage[ExitConditionRule]
    with ConditionRuleTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def conditionRuleItemStorage: ConditionRuleItemStorage

  override def create(sequencingId: Long, entity: ExitConditionRule): Unit = db.withSession { implicit s =>
    val ruleId = conditionRuleTQ.returning(conditionRuleTQ.map(_.id)).insert(entity.convert(sequencingId))
    entity.conditions.conditions.foreach(conditionRuleItemStorage.createCondition(ruleId, _))
  }

  override def getRules(sequencingId: Long): Seq[ExitConditionRule] = db.withSession { implicit s =>
    val ruleType = ConditionRuleType.Exit
    
    val conditionRules = conditionRuleTQ
      .filter(cr => cr.sequencingId === sequencingId && cr.ruleType === ruleType)
      .list

    val ids = conditionRules.map(_.id.get)
    val conditionsMap = conditionRuleItemStorage.getConditions(ids)

    conditionRules.map(cr => {
      val conditions = conditionsMap(cr.id.get)
      cr.convertExit(conditions)
    })
  }

  implicit class ExitConditionRuleToRow(entity: ExitConditionRule) {
    def convert(sequencingId: Long): ConditionRuleModel =
      ConditionRuleModel(
        None, sequencingId, entity.conditions.combination, ConditionRuleType.Exit, None
      )
  }

  implicit class ConditionRuleToModel(entity: ConditionRuleModel) {
    def convertExit(conditions: Seq[ConditionRuleItem]): ExitConditionRule = {
      val conditionSet = new RuleConditionSet(conditions, entity.combination)
      new ExitConditionRule(conditionSet)
    }
  }
}


abstract class PreConditionRuleStorageImpl(val db: JdbcBackend#DatabaseDef,
                                           val driver: JdbcProfile)
  extends ConditionRuleStorage[PreConditionRule]
    with ConditionRuleTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def conditionRuleItemStorage: ConditionRuleItemStorage

  override def create(sequencingId: Long, entity: PreConditionRule): Unit = db.withSession { implicit s =>
    val ruleId = conditionRuleTQ.returning(conditionRuleTQ.map(_.id)).insert(entity.convert(sequencingId))
    entity.conditions.conditions.foreach(conditionRuleItemStorage.createCondition(ruleId, _))
  }

  override def getRules(sequencingId: Long): Seq[PreConditionRule] = db.withSession { implicit s =>
    val ruleType = ConditionRuleType.Pre

    val conditionRules = conditionRuleTQ
      .filter(cr => cr.sequencingId === sequencingId && cr.ruleType === ruleType)
      .list

    val ids = conditionRules.map(_.id.get)
    val conditionsMap = conditionRuleItemStorage.getConditions(ids)

    conditionRules.map(cr => {
      val conditions = conditionsMap(cr.id.get)
      cr.convertPre(conditions)
    })
  }

  implicit class PreConditionRuleToRow(entity: PreConditionRule) {
    def convert(sequencingId: Long): ConditionRuleModel =
      ConditionRuleModel(
        None, sequencingId, entity.conditions.combination, ConditionRuleType.Pre, Option(entity.action.toString)
      )
  }
  implicit class ConditionRuleToModel(entity: ConditionRuleModel) {
    def convertPre(conditions: Seq[ConditionRuleItem]): PreConditionRule = {
      val conditionSet = new RuleConditionSet(conditions, entity.combination)
      new PreConditionRule(conditionSet, PreConditionAction.withName(entity.action.get))
    }
  }
}

abstract class PostConditionRuleStorageImpl(val db: JdbcBackend#DatabaseDef,
                                            val driver: JdbcProfile)
  extends ConditionRuleStorage[PostConditionRule]
    with ConditionRuleTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def conditionRuleItemStorage: ConditionRuleItemStorage

  override def create(sequencingId: Long, entity: PostConditionRule): Unit = db.withSession { implicit s =>
    val ruleId = conditionRuleTQ.returning(conditionRuleTQ.map(_.id)).insert(entity.convert(sequencingId))
    entity.conditions.conditions.foreach(conditionRuleItemStorage.createCondition(ruleId, _))
  }

  override def getRules(sequencingId: Long): Seq[PostConditionRule] = db.withSession { implicit s =>
    val ruleType = ConditionRuleType.Post

    val conditionRules = conditionRuleTQ
      .filter(cr => cr.sequencingId === sequencingId && cr.ruleType === ruleType)
      .list

    val ids = conditionRules.map(_.id.get)
    val conditionsMap = conditionRuleItemStorage.getConditions(ids)

    conditionRules.map(cr => {
      val conditions = conditionsMap(cr.id.get)
      cr.convertPost(conditions)
    })
  }

  implicit class PostConditionRuleToRow(entity: PostConditionRule) {
    def convert(sequencingId: Long): ConditionRuleModel =
      ConditionRuleModel(
        None, sequencingId, entity.conditions.combination, ConditionRuleType.Post, Option(entity.action.toString)
      )
  }
  implicit class ConditionRuleToModel(entity: ConditionRuleModel) {
    def convertPost(conditions: Seq[ConditionRuleItem]): PostConditionRule = {
      val conditionSet = new RuleConditionSet(conditions, entity.combination)
      new PostConditionRule(conditionSet, PostConditionAction.withName(entity.action.get))
    }
  }
}
