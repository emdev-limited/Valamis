package com.arcusys.valamis.web.listener

import com.arcusys.valamis.course.storage.CourseCertificateRepository
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.messaging.{Message, MessageListener}


class LearningPathDeletedMessageListener extends MessageListener
  with Injectable
  with LogSupport {
  val bindingModule: BindingModule = Configuration

  private lazy val courseCertificateRepository = inject[CourseCertificateRepository]

  override def receive(message: Message): Unit = {
    try {
      Option(message.getLong("learningPathId")) foreach { learningPathId =>
        courseCertificateRepository.deleteByCertificateId(learningPathId)
      }
    } catch {
      case e: Throwable =>
        log.error(s"Failed to handle learning path deleted event: " + message, e)
    }
  }
}