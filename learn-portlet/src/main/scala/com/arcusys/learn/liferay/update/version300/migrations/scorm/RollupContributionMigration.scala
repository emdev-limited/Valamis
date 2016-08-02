package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.lesson.scorm.model.manifest.{RandomizationTimingType, RollupConsiderationType}
import com.arcusys.valamis.lesson.scorm.model.manifest.RollupConsiderationType._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.RollupContributionModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{RollupContributionTableComponent, SequencingTableComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class RollupContributionMigration(val db: JdbcBackend#DatabaseDef,
                                  val driver: JdbcProfile)
  extends RollupContributionTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(seqOldId: Long, seqNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(seqOldId)


    if (entities.nonEmpty) {
      rollupContributionTQ ++= entities.map(_.copy(sequencingId = seqNewId))
    }
  }

  private def getOld(seqId: Long)(implicit s: JdbcBackend#Session): Seq[RollupContributionModel] = {
    implicit val reader = GetResult[RollupContributionModel](r => RollupContributionModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // sequencingID INTEGER null,
      r.nextStringOption().map(RollupConsiderationType.withName), // contributeToSatisfied TEXT null,
      r.nextStringOption().map(RollupConsiderationType.withName), // contributeToNotSatisfied TEXT null,
      r.nextStringOption().map(RollupConsiderationType.withName), // contributeToCompleted TEXT null,
      r.nextStringOption().map(RollupConsiderationType.withName), // contributeToIncomplete TEXT null,
      r.nextBigDecimal(), // objectiveMeasureWeight NUMERIC(20,2),
      r.nextBoolean() // measureSatisfactionIfActive BOOLEAN null
    ))

    StaticQuery.queryNA[RollupContributionModel](s"select * from Learn_LFRollupContribution where sequencingID = $seqId").list
  }
}
