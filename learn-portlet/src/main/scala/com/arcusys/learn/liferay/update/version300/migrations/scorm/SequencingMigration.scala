package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.SequencingModel
import com.arcusys.valamis.persistence.impl.scorm.schema.SequencingTableComponent
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class SequencingMigration(val db: JdbcBackend#DatabaseDef,
                          val driver: JdbcProfile)
  extends SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  val childrenSelectionMigration = new ChildrenSelectionMigration(db, driver)
  val sequencingTrackingMigration = new SequencingTrackingMigration(db, driver)
  val seqPermissionsMigration = new SeqPermissionsMigration(db, driver)
  val rollupContributionMigration = new RollupContributionMigration(db, driver)
  val rollupRuleMigration = new RollupRuleMigration(db, driver)
  val conditionRuleMigration = new ConditionRuleMigration(db, driver)
  val objectiveMigration = new ObjectiveMigration(db, driver)


  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val grades = getOldGrades


    if (grades.nonEmpty) {
      grades.foreach(a => {
       val newId = sequencingTQ.returning(sequencingTQ.map(_.id)).insert(a)
        childrenSelectionMigration.migrate(a.id.get, newId)
        sequencingTrackingMigration.migrate(a.id.get, newId)
        seqPermissionsMigration.migrate(a.id.get, newId)
        rollupContributionMigration.migrate(a.id.get, newId)
        rollupRuleMigration.migrate(a.id.get, newId)
        conditionRuleMigration.migrate(a.id.get, newId)
        objectiveMigration.migrate(a.id.get, newId)
      })
    }

  }

  private def getOldGrades(implicit s: JdbcBackend#Session): Seq[SequencingModel] = {
    implicit val reader = GetResult[SequencingModel](r => {
      val id = r.nextLongOption() // LONG not null primary key,
      val packageId = r.nextLongOption() // packageID INTEGER null,
      val activityId = r.nextStringOption() // activityID VARCHAR(512) null,
      val sharedId = r.nextStringOption() // sharedId TEXT null,
      val sharedSequencingIdReference = r.nextStringOption() // sharedSequencingIdReference TEXT null,
      val cAttemptObjectiveProgressChild = r.nextBoolean() // cAttemptObjectiveProgressChild BOOLEAN null,
      val cAttemptAttemptProgressChild = r.nextBoolean() // cAttemptAttemptProgressChild BOOLEAN null,
      val attemptLimit = r.nextIntOption() // attemptLimit INTEGER null,
      val durationLimitInMilliseconds = r.nextLongOption() // durationLimitInMilliseconds LONG null,
      r.nextLongOption()
      val preventChildrenActivation = r.nextBoolean() // preventChildrenActivation BOOLEAN null,
      val constrainChoice = r.nextBoolean() // constrainChoice BOOLEAN null
      SequencingModel(id,
        packageId,
        activityId,
        sharedId,
        sharedSequencingIdReference,
        cAttemptObjectiveProgressChild,
        cAttemptAttemptProgressChild,
        attemptLimit,
        durationLimitInMilliseconds,
        preventChildrenActivation,
        constrainChoice
      )
    })

    StaticQuery.queryNA[SequencingModel]("select * from Learn_LFSequencing").list
  }
}
