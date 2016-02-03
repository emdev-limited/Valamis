package com.arcusys.learn.notifications.services

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.notifications.CourseMessageService
import com.arcusys.valamis.lrs.service.IncorrectLrsSettingsException
import com.escalatesoft.subcut.inject.BindingModule
import org.quartz.{Job, JobExecutionContext}

class CourseNotificationSender extends Job with CourseMessageService {
  override implicit lazy val bindingModule: BindingModule = Configuration

  override def execute(ctx: JobExecutionContext) {
    try{
      sendCourseMessages()
      sendCertificateMessages()
    }
    catch {
      case e: IncorrectLrsSettingsException =>
    }
  }
}
