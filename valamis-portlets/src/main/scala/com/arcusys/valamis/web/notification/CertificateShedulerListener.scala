package com.arcusys.valamis.web.notification

import com.arcusys.valamis.certificate.service.{CertificateNotificationService, CertificateShedulerServiceImpl}
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.portlet.util.Logging
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.messaging.{Message, MessageListener}

class CertificateShedulerListener extends CertificateShedulerServiceImpl
  with Injectable
  with MessageListener
  with Logging {

  lazy val certificateRepository = inject[CertificateRepository]
  lazy val certificateNotifications = inject[CertificateNotificationService]

  implicit def bindingModule: BindingModule = Configuration

  override def receive(message: Message): Unit = {
    try {
      super.doAction()
    } catch {
      case e: Throwable =>
        println(e.getMessage)
    }
  }
}
