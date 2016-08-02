package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.lesson.scorm.model.manifest.{ConditionCombination, RandomizationTimingType, RollupAction}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.RollupRuleModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{RollupRuleTableComponent, SequencingTableComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class RollupRuleMigration(val db: JdbcBackend#DatabaseDef,
                          val driver: JdbcProfile)
  extends RollupRuleTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  val conditionRuleItemMigration = new ConditionRuleItemMigration(db, driver)

  def migrate(seqOldId: Long, seqNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(seqOldId)


    if (entities.nonEmpty) {
      entities.foreach(a => {
        val newId = rollupRuleTQ.returning(rollupRuleTQ.map(_.id)).insert(a.copy(sequencingId = seqNewId))
        conditionRuleItemMigration.migrate(false, a.id.get, newId)
      })
    }
  }

  private def getOld(seqId: Long)(implicit s: JdbcBackend#Session): Seq[RollupRuleModel] = {
    implicit val reader = GetResult[RollupRuleModel](r => RollupRuleModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // sequencingID INTEGER null,
      ConditionCombination.withName(r.nextString()), // combination TEXT null,
      r.nextString(), // childActivitySet TEXT null,
      r.nextIntOption(), // minimumCount INTEGER null,
      r.nextBigDecimalOption(), // minimumPercent NUMERIC(20,2),
      RollupAction.withName(r.nextString()) // action TEXT null
    ))

    StaticQuery.queryNA[RollupRuleModel](s"select * from Learn_LFRollupRule where sequencingID = $seqId").list
  }
}
