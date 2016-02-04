package com.arcusys.learn

import com.arcusys.learn.packages.PackageGradesRepositoryImpl
import com.arcusys.learn.scorm.course.impl.liferay.{CourseRepositoryImpl, PlayerScopeRuleRepositoryImpl}
import com.arcusys.learn.scorm.manifest.sequencing.storage.impl._
import com.arcusys.learn.scorm.manifest.storage.impl.liferay._
import com.arcusys.learn.scorm.sequencing.storage.impl.liferay._
import com.arcusys.learn.scorm.tracking.impl.liferay.UserStorageImpl
import com.arcusys.learn.scorm.tracking.states.impl.liferay._
import com.arcusys.learn.scorm.tracking.states.storage.impl.ActivityStateTreeCreator
import com.arcusys.learn.scorm.tracking.storage.impl.AttemptCreator
import com.arcusys.learn.scorm.tracking.storage.impl.liferay.{AttemptStorageImpl, DataModelStorageImpl}
import com.arcusys.learn.setting.storage.impl.SiteDependentSettingEntityStorage
import com.arcusys.learn.settings.model.LFSiteDependentSettingStorageImpl
import com.arcusys.learn.tincan.manifest.storage.impl.liferay.{TincanManifestActivityRepositoryImpl, TincanPackageRepositoryImpl}
import com.arcusys.learn.tincan.storage.impl.TincanURIEntityStorage
import com.arcusys.learn.tincan.storage.impl.liferay.LFTincanURIStorageImpl
import com.arcusys.valamis.grade.storage.{CourseGradeStorage, PackageGradesStorage}
import com.arcusys.valamis.lesson.scorm.model.manifest.{ExitConditionRule, PostConditionRule, PreConditionRule}
import com.arcusys.valamis.lesson.scorm.storage.sequencing._
import com.arcusys.valamis.lesson.scorm.storage.tracking._
import com.arcusys.valamis.lesson.scorm.storage.{ActivityDataStorage, ActivityStorage, ResourcesStorage, ScormPackagesStorage}
import com.arcusys.valamis.lesson.storage.{LessonLimitStorage, PackageScopeRuleStorage, PlayerScopeRuleStorage}
import com.arcusys.valamis.lesson.tincan.storage.{TincanManifestActivityStorage, TincanPackageStorage}
import com.arcusys.valamis.settings.storage.SiteDependentSettingStorage
import com.arcusys.valamis.uri.storage.TincanURIStorage
import com.arcusys.valamis.user.storage.UserStorage
import com.escalatesoft.subcut.inject.NewBindingModule

class PersistenceLFConfiguration extends NewBindingModule(implicit module => {
    import module._

    // Not related to tincan
    bind[ScormPackagesStorage].toProvider(module => new ScormPackageRepositoryImpl {
      val packageScopeRuleRepository = module.inject[PackageScopeRuleStorage](None)
    })

    bind[TincanPackageStorage].toProvider(module => new TincanPackageRepositoryImpl {
      val packageScopeRuleRepository = module.inject[PackageScopeRuleStorage](None)
    })

    bind[TincanManifestActivityStorage].toSingle(new TincanManifestActivityRepositoryImpl)

    bind[ActivityStorage].toProvider(module => new ActivityRepositoryImpl {
      val sequencingStorage = module.inject[SequencingStorage](None)
      val activityDataRepository = module.inject[ActivityDataStorage](None)
    })

    bind[ResourcesStorage] toSingle new ResourcesRepositoryImpl

    bind[AttemptStorage].toProvider(module => new AttemptStorageImpl with AttemptCreator {
      def userStorage: UserStorage = module.inject[UserStorage](None)

      def packageStorage = module.inject[ScormPackagesStorage](None)
    })

    bind[DataModelStorage] toSingle new DataModelStorageImpl
    bind[UserStorage] toSingle new UserStorageImpl
    bind[ActivityStateTreeStorage].toProvider(module => new ActivityStateTreeStorageImpl with ActivityStateTreeCreator {
      def activityStateStorage: ActivityStateStorage = module.inject[ActivityStateStorage](None)

      def activityStateNodeStorage: ActivityStateNodeStorage = module.inject[ActivityStateNodeStorage](None)

      def globalObjectiveStorage: GlobalObjectiveStorage = module.inject[GlobalObjectiveStorage](None)
    })
    bind[ActivityStateNodeStorage].toProvider(module => new ActivityStateNodeStorageImpl {
      def activityStateStorage: ActivityStateStorage = module.inject[ActivityStateStorage](None)
    })
    bind[GlobalObjectiveStorage] toSingle new GlobalObjectiveStorageImpl
    bind[ObjectiveStateStorage].toSingle(new ObjectiveStateStorageImpl)
    bind[ActivityStateStorage].toProvider(module => new ActivityStateStorageImpl {
      def activitiesStorage: ActivityStorage = module.inject[ActivityStorage](None)

      def objectiveStateStorage: ObjectiveStateStorage = module.inject[ObjectiveStateStorage](None)
    })

    bind[CourseGradeStorage] toSingle new CourseRepositoryImpl
    bind[PlayerScopeRuleStorage] toSingle new PlayerScopeRuleRepositoryImpl

    bind[SequencingStorage].toProvider(module => new SequencingEntityStorage with LFSequencingStorageImpl with SequencingCreator {
      val sequencingPermissionsStorage = module.inject[SequencingPermissionsStorage](None)
      val rollupContributionStorage = module.inject[RollupContributionStorage](None)
      val objectiveStorageStorage = module.inject[ObjectiveStorage](None)
      val childrenSelectionStorage = module.inject[ChildrenSelectionStorage](None)
      val sequencingTrackingStorage = module.inject[SequencingTrackingStorage](None)
      val rollupRuleStorage = module.inject[RollupRuleStorage](None)
      val exitConditionRuleStorageImpl = module.inject[ConditionRuleStorage[ExitConditionRule]](None)
      val preConditionRuleStorageImpl = module.inject[ConditionRuleStorage[PreConditionRule]](None)
      val postConditionRuleStorageImpl = module.inject[ConditionRuleStorage[PostConditionRule]](None)

      val objectiveMapStorage = module.inject[ObjectiveMapStorage](None)
      val ruleConditionStorage = module.inject[RuleConditionStorage](None)
    })

    bind[RuleConditionStorage] toSingle new RuleConditionEntityStorage with LFRuleConditionStorageImpl
    bind[SequencingPermissionsStorage] toSingle new SequencingPermissionsEntityStorage with LFSequencingPermissionsStorageImpl
    bind[RollupContributionStorage] toSingle new RollupContributionEntityStorage with LFRollupContributionStorageImpl
    bind[ObjectiveMapStorage] toSingle new ObjectiveMapEntityStorage with LFObjectiveMapStorageImpl
    bind[ObjectiveStorage].toProvider(module => new ObjectiveEntityStorage with LFObjectiveStorageImpl with ObjectiveEntityCreator {
      val mapStorage = module.inject[ObjectiveMapStorage](None)
    })

    bind[ChildrenSelectionStorage] toSingle new ChildrenSelectionEntityStorage with LFChildrenSelectionStorageImpl
    bind[SequencingTrackingStorage] toSingle new SequencingTrackingEntityStorage with LFSequencingTrackingStorageImpl
    bind[RollupRuleStorage].toProvider(module => new RollupRuleEntityStorage with LFRollupRuleStorageImpl with RollupRuleCreator {
      val ruleConditionStorage = module.inject[RuleConditionStorage](None)
    })
    bind[ConditionRuleStorage[ExitConditionRule]].toProvider(module => new ExitConditionRuleEntityStorage with LFExitConditionRuleStorageImpl with ExitConditionRuleCreator {
      val ruleConditionStorage = module.inject[RuleConditionStorage](None)
    })
    bind[ConditionRuleStorage[PreConditionRule]].toProvider(module => new PreConditionRuleEntityStorage with LFPreConditionRuleStorageImpl with PreConditionRuleCreator {
      val ruleConditionStorage = module.inject[RuleConditionStorage](None)
    })
    bind[ConditionRuleStorage[PostConditionRule]].toProvider(module => new PostConditionRuleEntityStorage with LFPostConditionRuleStorageImpl with PostConditionRuleCreator {
      def ruleConditionStorage = module.inject[RuleConditionStorage](None)
    })

    bind[ActivityDataStorage] toSingle new ActivityDataRepositoryImpl

    bind[SiteDependentSettingStorage] toSingle new SiteDependentSettingEntityStorage with LFSiteDependentSettingStorageImpl

    bind[TincanURIStorage] toSingle new TincanURIEntityStorage with LFTincanURIStorageImpl

    bind[PackageGradesStorage] toSingle new PackageGradesRepositoryImpl

    bind[LessonLimitStorage] toSingle new LessonLimitRepositoryImpl
})
