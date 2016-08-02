package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.valamis.content.storage._
import com.arcusys.valamis.content.storage.impl.{AnswerStorageImpl, CategoryStorageImpl, PlainTextStorageImpl, QuestionStorageImpl}
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.scorm.model.manifest.{ExitConditionRule, PostConditionRule, PreConditionRule}
import com.arcusys.valamis.lesson.scorm.storage._
import com.arcusys.valamis.lesson.scorm.storage.sequencing._
import com.arcusys.valamis.lesson.scorm.storage.tracking._
import com.arcusys.valamis.lrsEndpoint.storage.{LrsEndpointStorage, LrsTokenStorage}
import com.arcusys.valamis.persistence.common.{DatabaseLayer, Slick3DatabaseLayer, SlickDBInfo}
import com.arcusys.valamis.persistence.impl.file.FileRepositoryImpl
import com.arcusys.valamis.persistence.impl.lrs.{LrsEndpointStorageImpl, TokenRepositoryImpl}
import com.arcusys.valamis.persistence.impl.scorm.storage._
import com.arcusys.valamis.persistence.impl.settings.{ActivityToStatementStorageImpl, SettingStorageImpl, StatementToActivityStorageImpl}
import com.arcusys.valamis.persistence.impl.slide._
import com.arcusys.valamis.persistence.impl.social.{CommentRepositoryImpl, LikeRepositoryImpl}
import com.arcusys.valamis.persistence.impl.uri.TincanUriStorageImpl
import com.arcusys.valamis.settings.storage.{ActivityToStatementStorage, SettingStorage, StatementToActivityStorage}
import com.arcusys.valamis.slide.storage._
import com.arcusys.valamis.social.storage.{CommentRepository, LikeRepository}
import com.arcusys.valamis.uri.storage.TincanURIStorage
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

class PersistenceSlickConfiguration(dbInfo: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule({
  implicit module =>
    import module.bind
    import configuration.inject

    // we need bind here because binds in parent module not available here
    bind[SlickDBInfo].toSingle(dbInfo)

    bind[FileStorage].toSingle {
      new FileRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[LrsTokenStorage].toSingle {
      new TokenRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[CommentRepository].toSingle {
      new CommentRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[LikeRepository].toSingle {
      new LikeRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[SlideThemeRepositoryContract].toSingle {
      new SlideThemeRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[SlideSetRepository].toSingle {
      new SlideSetRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[SlideRepository].toSingle {
      new SlideRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[SlideElementRepository].toSingle {
      new SlideElementRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[SettingStorage].toSingle {
      new SettingStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[SlideElementPropertyRepository].toSingle{
      new SlideElementPropertyRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[SlidePropertyRepository].toSingle{
      new SlidePropertyRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[DeviceRepository].toSingle{
      new DeviceRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[StatementToActivityStorage] toSingle {
      new StatementToActivityStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[LrsEndpointStorage] toSingle {
      new LrsEndpointStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    //Content manager

    bind[DatabaseLayer].toSingle {
      new Slick3DatabaseLayer(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    //plain text
    bind[PlainTextStorage].toSingle {
      new PlainTextStorageImpl(dbInfo.databaseDef,dbInfo.slickProfile)
    }

    //categories
    bind[CategoryStorage].toSingle {
      new CategoryStorageImpl(dbInfo.databaseDef,dbInfo.slickProfile)
    }

    //questions
    bind[QuestionStorage].toSingle {
      new QuestionStorageImpl(dbInfo.databaseDef,dbInfo.slickProfile)
    }

    //answers
    bind[AnswerStorage].toSingle {
      new AnswerStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[TincanURIStorage] toSingle {
      new TincanUriStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[ActivityToStatementStorage] toSingle {
      new ActivityToStatementStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[ActivityStorage] toSingle {
      new ActivityStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val sequencingStorage = inject[SequencingStorage](None)
        lazy val activityDataStorage = inject[ActivityDataStorage](None)
      }
    }

    bind[ResourcesStorage] toSingle {
      new ResourcesStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[AttemptStorage] toSingle {
      new AttemptStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[DataModelStorage] toSingle {
      new DataModelStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[ActivityStateTreeStorage] toSingle {
      new ActivityStateTreeStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val activityStateStorage = inject[ActivityStateStorage](None)
        lazy val activityStateNodeStorage = inject[ActivityStateNodeStorage](None)
      }
    }
    bind[ActivityStateNodeStorage] toSingle {
      new ActivityStateNodeStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val activityStateStorage = inject[ActivityStateStorage](None)
      }
    }
    bind[GlobalObjectiveStorage] toSingle {
      new GlobalObjectiveStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
    bind[ObjectiveStateStorage] toSingle {
      new ObjectiveStateStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
    bind[ActivityStateStorage] toSingle {
      new ActivityStateStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val activityStorage = inject[ActivityStorage](None)
      }
    }

    bind[SequencingStorage] toSingle {
      new SequencingStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val sequencingTrackingStorage = inject[SequencingTrackingStorage](None)
        lazy val exitConditionRuleStorage = inject[ConditionRuleStorage[ExitConditionRule]](None)
        lazy val postConditionRuleStorage = inject[ConditionRuleStorage[PostConditionRule]](None)
        lazy val preConditionRuleStorage = inject[ConditionRuleStorage[PreConditionRule]](None)
        lazy val sequencingPermissionsStorage = inject[SequencingPermissionsStorage](None)
        lazy val rollupContributionStorage = inject[RollupContributionStorage](None)
        lazy val rollupRuleStorage = inject[RollupRuleStorage](None)
        lazy val childrenSelectionStorage = inject[ChildrenSelectionStorage](None)
        lazy val objectiveStorage = inject[ObjectiveStorage](None)
      }
    }

    bind[ConditionRuleItemStorage] toSingle {
      new ConditionRuleItemStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
    bind[SequencingPermissionsStorage] toSingle {
      new SequencingPermissionsStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
    bind[RollupContributionStorage] toSingle {
      new RollupContributionStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
    bind[ObjectiveMapStorage] toSingle {
      new ObjectiveMapStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
    bind[ObjectiveStorage] toSingle {
      new ObjectiveStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val objectiveMapStoragee = inject[ObjectiveMapStorage](None)
      }
    }

    bind[ChildrenSelectionStorage] toSingle {
      new ChildrenSelectionStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
    bind[SequencingTrackingStorage] toSingle {
      new SequencingTrackingStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {

      }
    }
    bind[RollupRuleStorage] toSingle {
      new RollupRuleStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val conditionRuleItemStorage = inject[ConditionRuleItemStorage](None)
      }
    }
    bind[ConditionRuleStorage[ExitConditionRule]] toSingle {
      new ExitConditionRuleStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val conditionRuleItemStorage = inject[ConditionRuleItemStorage](None)
      }
    }
    bind[ConditionRuleStorage[PreConditionRule]] toSingle {
      new PreConditionRuleStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val conditionRuleItemStorage = inject[ConditionRuleItemStorage](None)
      }
    }
    bind[ConditionRuleStorage[PostConditionRule]] toSingle {
      new PostConditionRuleStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile) {
        lazy val conditionRuleItemStorage = inject[ConditionRuleItemStorage](None)
      }
    }

    bind[ActivityDataStorage] toSingle {
      new ActivityDataStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[ScormUserStorage] toSingle {
      new UserStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }
})