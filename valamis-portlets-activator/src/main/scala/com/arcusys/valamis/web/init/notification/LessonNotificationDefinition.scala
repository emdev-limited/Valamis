package com.arcusys.valamis.web.init.notification

import java.util.Locale
import com.arcusys.learn.liferay.util.{LanguageHelper, PortletName}
import com.liferay.portal.kernel.notifications.{UserNotificationDefinition, UserNotificationDeliveryType}

class LessonNotificationDefinition
  extends UserNotificationDefinition(PortletName.LessonViewer.key,
    0L,
    0,
    "notification.lesson") {
    //TODO: add localization

    this.addUserNotificationDeliveryType(new UserNotificationDeliveryType("website", 10002, true, true))

    override def getDescription(locale: Locale): String = LanguageHelper.get("notification.lesson")
}