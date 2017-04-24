package com.arcusys.valamis.web.notification

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.{LanguageHelper, PortletName}
import com.arcusys.valamis.certificate.model.CertificateNotificationModel
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper


class CertificateNotificationHandler extends LBaseUserNotificationHandler {
  setPortletId(PortletName.CertificateManager.key)

  override protected def getLink(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[CertificateNotificationModel](userNotificationEvent.getPayload)
    notification.certificateLink
  }

  override protected def getBody(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[CertificateNotificationModel](userNotificationEvent.getPayload)

    val userLocale = UserLocalServiceHelper().getUser(notification.userId).getLocale
    val tpl = LanguageHelper.get(userLocale, s"certificate.${notification.messageType}")

    val mustached = new Mustache(tpl)
    mustached.render(getParams(notification))
  }

  private def getParams(notification: CertificateNotificationModel) = {
    Map(
      "title" -> notification.certificateTitle
    )
  }
}
