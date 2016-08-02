package com.arcusys.valamis.web.notification

import com.liferay.portal.kernel.notifications.UserNotificationDefinition
import com.liferay.portal.kernel.notifications.UserNotificationDeliveryType

class GradebookNotificationDefinition
  extends UserNotificationDefinition(
    "com_arcusys_valamis_web_portlet_GradebookView",
    0L,
    0,
    "grades or comments your profile") { //TODO: add localization

    this.addUserNotificationDeliveryType(new UserNotificationDeliveryType("website", 10002, true, true))

}
