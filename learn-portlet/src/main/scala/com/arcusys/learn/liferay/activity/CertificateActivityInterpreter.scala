package com.arcusys.learn.liferay.activity

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LBaseSocialActivityInterpreter
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.StringPoolHelper
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.lesson.model.CertificateActivityType
import com.escalatesoft.subcut.inject.Injectable

object CertificateActivityInterpreter {
  val className = Array(classOf[Certificate].getName, CertificateActivityType.getClass.getName, CertificateStateType.getClass.getName)
}

class CertificateActivityInterpreter extends LBaseSocialActivityInterpreter with Injectable {
  implicit lazy val bindingModule = Configuration
  lazy val certificateRepository = inject[CertificateRepository]

  override protected def doInterpret(activity: LSocialActivity, context: Context): LSocialActivityFeedEntry = {
    def interpretCertificate = {
      val creatorUserName = getUserName(activity.getUserId, context)
      val activityType: Int = activity.getType

      val title = activity.getClassName match {
        case className if (className == CertificateActivityInterpreter.className(0)) =>
          if (activityType == CertificateStatuses.Success.id) "achieved a certificate"
        case className if (className == CertificateActivityInterpreter.className(1)) => "joined a certificate"
        case className if (className == CertificateActivityInterpreter.className(2)) =>
          if (activityType == CertificateStateType.Publish.id) "published a certificate"
      }

      val certificate = certificateRepository.getById(activity.getClassPK.toInt)
      val sb = new StringBuilder
      sb.append(creatorUserName + " ")
      sb.append(title + " ")
      sb.append(certificate.title)
      new LSocialActivityFeedEntry(StringPoolHelper.BLANK, sb.toString(), StringPoolHelper.BLANK)
    }

    interpretCertificate
  }

  def getClassNames() = CertificateActivityInterpreter.className
}
