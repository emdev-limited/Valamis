package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.util.LanguageHelper
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.course._
import com.arcusys.valamis.course.service._
import com.arcusys.valamis.course.storage.{CourseCertificateRepository, CourseExtendedRepository, CourseInstructorRepository}
import com.arcusys.valamis.file.service.{FileEntryService, FileEntryServiceImpl, FileService, FileServiceImpl}
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.settings.SettingStorage
import com.arcusys.valamis.settings.service.{LRSToActivitySettingService, LRSToActivitySettingServiceImpl, SettingService, SettingServiceImpl}
import com.arcusys.valamis.settings.storage.StatementToActivityStorage
import com.arcusys.valamis.slide.service.contentProvider.ContentProviderService
import com.arcusys.valamis.slide.service.contentProvider.impl.ContentProviderServiceImpl
import com.arcusys.valamis.slide.service.lti.LTIDataService
import com.arcusys.valamis.slide.service.lti.impl.LTIDataServiceImpl
import com.arcusys.valamis.slide.storage.{ContentProviderRepository, LTIDataRepository}
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.uri.service.{TincanURIService, TincanURIServiceImpl}
import com.arcusys.valamis.uri.storage.TincanURIStorage
import com.arcusys.valamis.user.service.{UserCertificateRepository, UserService, UserServiceImpl}
import com.arcusys.valamis.web.util.ForkJoinPoolWithLRCompany
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 26.02.16.
  */
class CommonConfiguration(db: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule(module => {
  import configuration.inject
  import module.bind

  bind[TagService[LGroup]].toSingle(new TagService[LGroup])

  bind[CourseNotificationService].toSingle(new CourseNotificationServiceImpl)

  bind[FileEntryService].toSingle(new FileEntryServiceImpl)

  bind[FileService] toSingle new FileServiceImpl {
    lazy val fileStorage = inject[FileStorage](None)
  }

  bind[LRSToActivitySettingService] toSingle new LRSToActivitySettingServiceImpl {
    lazy val lrsToActivitySettingStorage = inject[StatementToActivityStorage](None)
  }

  bind[TincanURIService] toSingle new TincanURIServiceImpl {
    lazy val uriStorage = inject[TincanURIStorage](None)
  }

  bind[UserService] toSingle new UserServiceImpl {
    def removedUserPrefix = LanguageHelper.get("deleted-user")
    lazy val userCertificateRepository = inject[UserCertificateRepository](None)
  }

  bind[SettingService] toSingle new SettingServiceImpl {
    lazy val settingStorage = inject[SettingStorage](None)
  }

  bind[api.CourseService] toSingle new api.CourseServiceImpl {
    lazy val courseTagService = inject[TagService[LGroup]](None)
    lazy val courseRepository = inject[CourseExtendedRepository](None)
    lazy val courseMemberService = inject[CourseMemberService](None)
    lazy val courseCertificateRepository = inject[CourseCertificateRepository](None)
  }

  bind[CourseService] toSingle new CourseServiceImpl {
    lazy val courseTagService = inject[TagService[LGroup]](None)
    lazy val courseRepository = inject[CourseExtendedRepository](None)
    lazy val courseMemberService = inject[CourseMemberService](None)
    lazy val courseCertificateRepository = inject[CourseCertificateRepository](None)
  }

  bind[InstructorService] toSingle new InstructorServiceImpl {
    lazy val instructorRepository = inject[CourseInstructorRepository](None)
  }

  bind[CourseMemberService] toSingle new CourseMemberServiceImpl {
    lazy val memberService = inject[MemberService](None)
    lazy val courseNotification = inject[CourseNotificationService](None)
    lazy val courseRepository = inject[CourseExtendedRepository](None)
    lazy val courseUserQueueService = inject[CourseUserQueueService](None)
  }

  bind[ContentProviderService] toSingle new ContentProviderServiceImpl {
    lazy val contentProviderRepository = inject[ContentProviderRepository](None)
  }


  bind[LTIDataService] toSingle new LTIDataServiceImpl(db) {
    val executionContext: ExecutionContext = ForkJoinPoolWithLRCompany.ExecutionContext
    lazy val ltiDataRepository = inject[LTIDataRepository](None)
  }

  bind[CourseUserQueueService] toSingle new CourseUserQueueServiceImpl(db.databaseDef, db.slickDriver) {

  }
})
