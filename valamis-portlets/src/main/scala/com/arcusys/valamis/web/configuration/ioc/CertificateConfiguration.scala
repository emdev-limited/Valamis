package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.certificate.reports.{DateReport, DateReportImpl}
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.certificate.service.export.{CertificateExportProcessor, CertificateImportProcessor}
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.certificate.storage.repository._
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.gradebook.service.{LessonGradeService, TeacherCourseGradeService}
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader, TeacherLessonGradeService, UserLessonResultService}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.settings.storage.SettingStorage
import com.arcusys.valamis.user.service.{UserCertificateRepository, UserService}
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

class CertificateConfiguration(db: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule(module => {
  import configuration.inject
  import module.bind

  bind[DateReport] toSingle new DateReportImpl() {
    lazy val certificateHistory = inject[CertificateHistoryService](None)
    lazy val userStatusHistory = inject[UserStatusHistoryService](None)
  }

  bind[CertificateHistoryService] toSingle {
    new CertificateHistoryServiceImpl(db.databaseDef, db.slickProfile)
  }

  bind[UserStatusHistoryService] toSingle {
    new UserStatusHistoryServiceImpl(db.databaseDef, db.slickProfile)
  }

  bind[CertificateService] toSingle new CertificateServiceImpl {
    lazy val certificateRepository = inject[CertificateRepository](None)
    lazy val certificateStateRepository = inject[CertificateStateRepository](None)
    lazy val goalStateRepository = inject[CertificateGoalStateRepository](None)
    lazy val fileService = inject[FileService](None)
    lazy val certificateUserService = inject[CertificateUserService](None)
    lazy val certificateMemberService = inject[CertificateMemberService](None)
    lazy val certificateGoalService = inject[CertificateGoalService](None)
    lazy val checker = inject[CertificateStatusChecker](None)

    lazy val certificateHistory = inject[CertificateHistoryService](None)

    lazy val userStatusHistory = inject[UserStatusHistoryService](None)
    lazy val certificateNotification = inject[CertificateNotificationService](None)
  }

  bind[CertificateUserService] toSingle new CertificateUserServiceImpl {
    lazy val certificateRepository = inject[CertificateRepository](None)
    lazy val certificateToUserRepository = inject[CertificateStateRepository](None)
    lazy val goalStateRepository = inject[CertificateGoalStateRepository](None)
    lazy val checker = inject[CertificateStatusChecker](None)
    lazy val certificateMemberService = inject[CertificateMemberService](None)
    lazy val certificateBadgeService = inject[CertificateBadgeService](None)
    lazy val certificateGoalService = inject[CertificateGoalService](None)
    lazy val userStatusHistory = inject[UserStatusHistoryService](None)
    lazy val certificateService = inject[CertificateService](None)
    lazy val certificateNotification = inject[CertificateNotificationService](None)
  }

  bind[CertificateNotificationService] toSingle new CertificateNotificationServiceImpl {
    lazy val certificateRepository = inject[CertificateRepository](None)
    lazy val certificateService = inject[CertificateService](None)
  }

  bind[CertificateGoalService] toSingle new CertificateGoalServiceImpl {
    lazy val courseGoalStorage = inject[CourseGoalStorage](None)
    lazy val activityGoalStorage = inject[ActivityGoalStorage](None)
    lazy val statementGoalStorage = inject[StatementGoalStorage](None)
    lazy val packageGoalStorage = inject[PackageGoalStorage](None)
    lazy val assignmentGoalStorage = inject[AssignmentGoalStorage](None)
    lazy val checker = inject[CertificateStatusChecker](None)
    lazy val certificateRepository = inject[CertificateRepository](None)
    lazy val lessonService = inject[LessonService](None)
    lazy val certificateStateRepository = inject[CertificateStateRepository](None)
    lazy val goalRepository = inject[CertificateGoalRepository](None)
    lazy val certificateGoalRepository = inject[CertificateGoalRepository](None)
    lazy val certificateGoalGroupRepository = inject[CertificateGoalGroupRepository](None)
    lazy val teacherGradeService = inject[TeacherLessonGradeService](None)
    lazy val gradeService = inject[LessonGradeService](None)
    lazy val assignmentService = inject[AssignmentService](None)
    lazy val goalStateRepository = inject[CertificateGoalStateRepository](None)
    lazy val packageGoalRepository = inject[PackageGoalStorage](None)
    lazy val goalGroupRepository = inject[CertificateGoalGroupRepository](None)
    lazy val userStatusHistory = inject[UserStatusHistoryService](None)
  }

  bind[CertificateBadgeService] toSingle new CertificateBadgeServiceImpl {
    lazy val certificateRepository = inject[CertificateRepository](None)
    lazy val userLocalServiceHelper = inject[UserLocalServiceHelper](None)
    lazy val settingStorage = inject[SettingStorage](None)
    lazy val userService = inject[UserService](None)
  }

  bind[CertificateMemberService] toSingle new CertificateMemberServiceImpl {
    lazy val certificateMemberRepository = inject[CertificateMemberRepository](None)
    lazy val memberService = inject[MemberService](None)
    lazy val certificateStateRepository = inject[CertificateStateRepository](None)
    lazy val userService = inject[UserService](None)
  }

  bind[CertificateStatusChecker] toSingle new CertificateStatusCheckerImpl {
    lazy val certificateRepository = inject[CertificateRepository](None)
    lazy val certificateSocialActivityHelper = new SocialActivityHelper[Certificate]
    lazy val activityGoalStorage = inject[ActivityGoalStorage](None)
    lazy val certificateStateRepository = inject[CertificateStateRepository](None)
    lazy val goalStateRepository = inject[CertificateGoalStateRepository](None)
    lazy val courseGoalStorage = inject[CourseGoalStorage](None)
    lazy val courseGradeStorage = inject[TeacherCourseGradeService](None)
    lazy val packageGoalStorage = inject[PackageGoalStorage](None)
    lazy val assignmentGoalStorage = inject[AssignmentGoalStorage](None)
    lazy val lessonService = inject[LessonService](None)
    lazy val statementReader = inject[LessonStatementReader](None)
    lazy val statementGoalStorage = inject[StatementGoalStorage](None)
    lazy val lrsClient = inject[LrsClientManager](None)
    lazy val certificateGoalGroupRepository = inject[CertificateGoalGroupRepository](None)
    lazy val goalRepository = inject[CertificateGoalRepository](None)
    lazy val gradeService = inject[LessonGradeService](None)
    lazy val lessonResultService = inject[UserLessonResultService](None)
    lazy val teacherGradeService = inject[TeacherLessonGradeService](None)
    lazy val assignmentService = inject[AssignmentService](None)
    //have to use Configuration to have access to bindings added in additional configurations
    lazy val userLocalServiceHelper = inject[UserLocalServiceHelper](None)
    lazy val userStatusHistory = inject[UserStatusHistoryService](None)
    lazy val certificateNotification = inject[CertificateNotificationService](None)
  }

  bind[CertificateImportProcessor] toSingle new CertificateImportProcessor {
    lazy val courseService = inject[CourseService](None)
    lazy val certificateService = inject[CertificateService](None)
    lazy val courseGoalStorage = inject[CourseGoalStorage](None)
    lazy val activityGoalStorage = inject[ActivityGoalStorage](None)
    lazy val statementGoalStorage = inject[StatementGoalStorage](None)
    lazy val packageGoalStorage = inject[PackageGoalStorage](None)
  }

  bind[CertificateExportProcessor] toSingle new CertificateExportProcessor {
    lazy val fileService = inject[FileService](None)
    lazy val courseService = inject[CourseService](None)
    lazy val courseGoalStorage = inject[CourseGoalStorage](None)
    lazy val activityGoalStorage = inject[ActivityGoalStorage](None)
    lazy val statementGoalStorage = inject[StatementGoalStorage](None)
    lazy val packageGoalStorage = inject[PackageGoalStorage](None)
    lazy val goalRepository = inject[CertificateGoalRepository](None)
    lazy val certificateRepository = inject[CertificateRepository](None)
  }

  bind[CourseGoalStorage].toSingle {
    new CourseGoalStorageImpl(db.databaseDef, db.slickProfile){
      lazy val certificateGoalRepository = inject[CertificateGoalRepository](None)
    }
  }

  bind[PackageGoalStorage].toSingle {
    new PackageGoalStorageImpl(db.databaseDef, db.slickProfile){
      lazy val certificateGoalRepository = inject[CertificateGoalRepository](None)
    }
  }

  bind[ActivityGoalStorage].toSingle {
    new ActivityGoalStorageImpl(db.databaseDef, db.slickProfile) {
      lazy val certificateGoalRepository = inject[CertificateGoalRepository](None)
    }
  }

  bind[StatementGoalStorage].toSingle {
    new StatementGoalStorageImpl(db.databaseDef, db.slickProfile){
      lazy val certificateGoalRepository = inject[CertificateGoalRepository](None)
    }
  }

  bind[AssignmentGoalStorage].toSingle {
    new AssignmentGoalStorageImpl(db.databaseDef, db.slickProfile) {
      lazy val certificateGoalRepository = inject[CertificateGoalRepository](None)
    }
  }

  bind[CertificateRepository].toSingle {
    new CertificateRepositoryImpl(db.databaseDef, db.slickProfile)
  }

  bind[CertificateStateRepository].toSingle {
    new CertificateStateRepositoryImpl(db.databaseDef, db.slickProfile)
  }

  bind[CertificateGoalStateRepository] toSingle {
    new CertificateGoalStateRepositoryImpl(db.databaseDef, db.slickProfile)
  }

  bind[CertificateMemberRepository] toSingle {
    new CertificateMemberRepositoryImpl(db.databaseDef, db.slickProfile)
  }

  bind[CertificateGoalRepository] toSingle {
    new CertificateGoalRepositoryImpl(db.databaseDef, db.slickProfile)
  }

  bind[UserCertificateRepository].toSingle {
    new CertificateStateRepositoryImpl(db.databaseDef, db.slickProfile) {
      lazy val certificateRepository = inject[CertificateRepository](None)
    }
  }

  bind[CertificateGoalGroupRepository].toSingle {
    new CertificateGoalGroupRepositoryImpl(db.databaseDef, db.slickProfile) {
      lazy val goalRepository = inject[CertificateGoalRepository](None)
    }
  }
})
