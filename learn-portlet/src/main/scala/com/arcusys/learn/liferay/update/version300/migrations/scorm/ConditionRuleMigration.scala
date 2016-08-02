package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionRuleType._
import com.arcusys.valamis.lesson.scorm.model.manifest.{ConditionCombination, ConditionRuleType, RandomizationTimingType}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ConditionRuleModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ConditionRuleTableComponent, SequencingTableComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ConditionRuleMigration(val db: JdbcBackend#DatabaseDef,
                             val driver: JdbcProfile)
  extends ConditionRuleTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  val conditionRuleItemMigration = new ConditionRuleItemMigration(db, driver)


  def migrate(seqOldId: Long, seqNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(seqOldId)


    if (entities.nonEmpty) {
      entities.foreach(a => {
        val newId = conditionRuleTQ.returning(conditionRuleTQ.map(_.id)).insert(a.copy(sequencingId = seqNewId))
        conditionRuleItemMigration.migrate(true, a.id.get, newId)
      })
    }

  }

  private def getOld(seqId: Long)(implicit s: JdbcBackend#Session): Seq[ConditionRuleModel] = {
    implicit val reader = GetResult[ConditionRuleModel](r => ConditionRuleModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // sequencingID INTEGER null,
      ConditionCombination.withName(r.nextString()), // combination TEXT null,
      ConditionRuleType.withName(r.nextString()), // ruleType VARCHAR(3000) null,
      r.nextStringOption() // action TEXT null
    ))

    StaticQuery.queryNA[ConditionRuleModel](s"select * from Learn_LFConditionRule where sequencingID = $seqId").list
  }
}
