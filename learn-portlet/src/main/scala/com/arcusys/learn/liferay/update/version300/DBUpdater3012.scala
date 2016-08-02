package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.migrations.scorm._
import com.arcusys.valamis.persistence.impl.scorm.schema.{SequencingTrackingTableComponent, _}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3012(val bindingModule: BindingModule)
  extends LUpgradeProcess
  with ActivityDataMapTableComponent
  with ActivityStateNodeTableComponent
  with AttemptDataTableComponent
  with ActivityStateTreeTableComponent
  with ActivityStateTableComponent
  with ActivityTableComponent
  with AttemptTableComponent
  with ChildrenSelectionTableComponent
  with ConditionRuleItemTableComponent
  with ConditionRuleTableComponent
  with GlblObjectiveStateTableComponent
  with ObjectiveMapTableComponent
  with ObjectiveStateTableComponent
  with ObjectiveTableComponent
  with ResourceTableComponent
  with RollupContributionTableComponent
  with RollupRuleTableComponent
  with ScormUserComponent
  with SeqPermissionsTableComponent
  with SequencingTableComponent
  with SequencingTrackingTableComponent
  with SlickDBContext{

  val logger = LogFactoryHelper.getLog(getClass)
  override def getThreshold = 3012

  def this() = this(Configuration)

  override def doUpgrade(): Unit = db.withTransaction{ implicit s =>
    try {
      import driver.simple._
      logger.info("Creating LEARN_SCO_ACTIVITY")
      activityTQ.ddl.create

      logger.info("Creating LEARN_SCO_ACTIVITY_DATA")
      activityDataMapTQ.ddl.create

      logger.info("Creating LEARN_SCO_ACTIVITY_STATE_NODE")
      activityStateNodeTQ.ddl.create

      logger.info("Creating LEARN_SCO__SCORM_USER")
      scormUsersTQ.ddl.create

      logger.info("Creating LEARN_SCO_ATTEMPT")
      attemptTQ.ddl.create

      logger.info("Creating LEARN_SCO_ACTIVITY")
      activityStateTreeTQ.ddl.create

      logger.info("Creating LEARN_SCO_ACTIVITY_STATE")
      activityStateTQ.ddl.create

      logger.info("Creating LEARN_SCO_ATTEMPT_DATA")
      attemptDataTQ.ddl.create

      logger.info("Creating LEARN_SCO_CHILDREN_SELECTION")
      childrenSelectionTQ.ddl.create

      logger.info("Creating LEARN_SCO_SEQUENCING")
      sequencingTQ.ddl.create

      logger.info("Creating LEARN_SCO_ROLLUP_RULE")
      rollupRuleTQ.ddl.create

      logger.info("Creating LEARN_SCO_CONDITION_RULE")
      conditionRuleTQ.ddl.create

      logger.info("Creating LEARN_SCO_CONDITION_RULE_ITEM")
      conditionRuleItemTQ.ddl.create

      logger.info("Creating LEARN_SCO_GLBL_OBJECTIVE_STATE")
      glblObjectiveStateTQ.ddl.create

      logger.info("Creating LEARN_SCO_OBJECTIVE")
      objectiveTQ.ddl.create

      logger.info("Creating LEARN_SCO_OBJECTIVE_STATE")
      objectiveStateTQ.ddl.create

      logger.info("Creating LEARN_SCO_OBJECTIVE_MAP")
      objectiveMapTQ.ddl.create

      logger.info("Creating LEARN_SCO_RESOURCE")
      resourceTQ.ddl.create

      logger.info("Creating LEARN_SCO_ROLLUP_CONTRIBUTION")
      rollupContributionTQ.ddl.create

      logger.info("Creating LEARN_SCO_SEQ_PERMISSIONS")
      seqPermissionsTQ.ddl.create

      logger.info("Creating LEARN_SCO_SEQUENCING_TRACKING")
      sequencingTrackingTQ.ddl.create

      logger.info("began ActivityDataMapMigration")
      new ActivityDataMapMigration(db, driver).migrate()

      logger.info("began ActivityStateNodeMigration")
      new ActivityStateNodeMigration(db, driver).migrate()

      logger.info("began ScormUserMigration")
      new ScormUserMigration(db, driver).migrate()

      logger.info("began AttemptMigration")
      new AttemptMigration(db, driver).migrate()

      logger.info("began ActivityStateTreeMigration")
      new ActivityStateTreeMigration(db, driver).migrate()

      logger.info("began ActivityMigration")
      new ActivityMigration(db, driver).migrate()

      logger.info("began SequencingMigration")
      new SequencingMigration(db, driver).migrate()

      logger.info("began GlblObjectiveStateMigration")
      new GlblObjectiveStateMigration(db, driver).migrate()

      logger.info("began ResourceMigration")
      new ResourceMigration(db, driver).migrate()
    }
    catch {
      case e: Throwable =>
        s.rollback()
        throw e
    }
  }
}
