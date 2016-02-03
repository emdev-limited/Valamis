package com.arcusys.learn.ioc

import com.arcusys.valamis.certificate.repository.{ActivityGoalStorageImpl, CertificateRepositoryImpl, CourseGoalStorageImpl, PackageGoalStorageImpl, _}
import com.arcusys.valamis.certificate.storage.{ActivityGoalStorage, CertificateRepository, CourseGoalStorage, PackageGoalStorage, _}
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.valamis.course.UserCourseResultStorageImpl
import com.arcusys.valamis.course.storage.UserCourseResultStorage
import com.arcusys.valamis.file.FileRepositoryImpl
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.{PackageScopeRuleStorageImpl, PackageCategoryGoalStorageImpl}
import com.arcusys.valamis.lesson.storage.PackageScopeRuleStorage
import com.arcusys.valamis.lesson.tincan.storage.PackageCategoryGoalStorage
import com.arcusys.valamis.lrs.{LrsEndpointStorageImpl, TokenRepositoryImpl}
import com.arcusys.valamis.lrsEndpoint.storage.{LrsEndpointStorage, LrsTokenStorage}
import com.arcusys.valamis.settings.{StatementToActivityStorageImpl, SettingStorageImpl}
import com.arcusys.valamis.settings.storage.{StatementToActivityStorage, SettingStorage}
import com.arcusys.valamis.slide.storage._
import com.arcusys.valamis.slide._
import com.arcusys.valamis.social.repository.{CommentRepositoryImpl, LikeRepositoryImpl}
import com.arcusys.valamis.social.storage.{CommentRepository, LikeRepository}
import com.arcusys.valamis.user.service.UserCertificateRepository
import com.escalatesoft.subcut.inject.NewBindingModule

import com.arcusys.valamis.content.storage._

class PersistenceSlickConfiguration(dbInfo: => SlickDBInfo) extends NewBindingModule({
  implicit module =>
    import module._

    // we need bind here because binds in parent module not available here
    bind[SlickDBInfo].toSingle(dbInfo)

    bind[FileStorage].toSingle {
      new FileRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[LrsTokenStorage].toSingle {
      new TokenRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[CourseGoalStorage].toSingle {
      new CourseGoalStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[PackageGoalStorage].toSingle {
      new PackageGoalStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[ActivityGoalStorage].toSingle {
      new ActivityGoalStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[StatementGoalStorage].toSingle {
      new StatementGoalStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[CertificateRepository].toSingle {
      new CertificateRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[CertificateStateRepository].toSingle {
      new CertificateStateRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[CommentRepository].toSingle {
      new CommentRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[LikeRepository].toSingle {
      new LikeRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[PackageGoalStorage].toSingle {
      new PackageGoalStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[PackageCategoryGoalStorage].toSingle {
      new PackageCategoryGoalStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
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

    bind[UserCertificateRepository].toSingle {
      new CertificateStateRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[CertificateStateRepository].toSingle {
      new CertificateStateRepositoryImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

    bind[UserCourseResultStorage].toSingle(
      new UserCourseResultStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    )

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

    bind[PackageScopeRuleStorage] toSingle {
      new PackageScopeRuleStorageImpl(dbInfo.databaseDef, dbInfo.slickProfile)
    }

})