package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.certificate.storage.repository.{CertificateGoalGroupRepositoryImpl, _}
import com.arcusys.valamis.gradebook.service.{LessonGradeService, TeacherCourseGradeService}
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader, TeacherLessonGradeService, UserLessonResultService}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.user.service.UserCertificateRepository
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

class CertificateConfiguration(db: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule(module => {
  import configuration.inject
  import module.bind

  bind[CertificateService] toSingle new CertificateServiceImpl

  bind[CertificateMemberService] toSingle new CertificateMemberServiceImpl

  bind[CertificateStatusChecker] toSingle new CertificateStatusCheckerImpl {
    lazy val certificateStorage = inject[CertificateRepository](None)
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
    lazy val userLocalServiceHelper = inject[UserLocalServiceHelper](None)
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
    new CertificateStateRepositoryImpl(db.databaseDef, db.slickProfile)
  }

  bind[CertificateGoalGroupRepository].toSingle {
    new CertificateGoalGroupRepositoryImpl(db.databaseDef, db.slickProfile) {
      lazy val goalRepository = inject[CertificateGoalRepository](None)
    }
  }
})
