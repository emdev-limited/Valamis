package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.reports.{DateReport, DateReportImpl}
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.certificate.storage._

import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.settings.SettingStorage
import com.arcusys.valamis.settings.service.SettingService
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

  bind[CertificateUserService] toSingle new CertificateUserServiceImpl {
    lazy val certificateBadgeService = inject[CertificateBadgeService](None)
  }

  bind[CertificateBadgeService] toSingle new CertificateBadgeServiceImpl {
    lazy val userLocalServiceHelper = inject[UserLocalServiceHelper](None)
    lazy val settingService = inject[SettingService](None)
    lazy val userService = inject[UserService](None)
    lazy val learningPathService = inject[LearningPathService](None)
  }
})
