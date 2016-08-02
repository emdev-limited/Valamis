package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionRuleType.ConditionRuleType
import com.arcusys.valamis.lesson.scorm.model.manifest.{ConditionRuleType, ConditionType}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ConditionRuleItemModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ConditionRuleItemTableComponent, ConditionRuleTableComponent, RollupRuleTableComponent, SequencingTableComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ConditionRuleItemMigration(val db: JdbcBackend#DatabaseDef,
                                 val driver: JdbcProfile)
  extends ConditionRuleItemTableComponent
    with ConditionRuleTableComponent
    with RollupRuleTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(isCondition: Boolean, ruleOldId: Long, ruleNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(isCondition, ruleOldId)


    if (entities.nonEmpty) {
      if(isCondition)
        conditionRuleItemTQ ++= entities.map(_.copy(conditionRuleId = Some(ruleNewId)))
      else
        conditionRuleItemTQ ++= entities.map(_.copy(rollupRuleId = Some(ruleNewId)))
    }

  }

  private def getOld(isCondition: Boolean, ruleOldId: Long)(implicit s: JdbcBackend#Session): Seq[ConditionRuleItemModel] = {
    implicit val reader = GetResult[ConditionRuleItemModel](r => ConditionRuleItemModel(
      r.nextLongOption(), // LONG not null primary key,
      ConditionType.withName(r.nextString()), // conditionType TEXT null,
      r.nextStringOption(), // objectiveId TEXT null,
      r.nextBigDecimalOption(), // measureThreshold NUMERIC(20,2),
      r.nextBoolean(), // inverse BOOLEAN null,
      r.nextLongOption(), // rollupRuleID INTEGER null,
      r.nextLongOption() // conditionRuleID INTEGER null
    ))

    if(isCondition)
      StaticQuery.queryNA[ConditionRuleItemModel](s"select * from Learn_LFRuleCondition where conditionRuleID = $ruleOldId").list
    else
      StaticQuery.queryNA[ConditionRuleItemModel](s"select * from Learn_LFRuleCondition where rollupRuleID = $ruleOldId").list
  }
}
