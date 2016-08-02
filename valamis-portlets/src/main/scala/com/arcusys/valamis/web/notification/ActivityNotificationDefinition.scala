package com.arcusys.valamis.web.notification

import com.liferay.portal.kernel.notifications.{UserNotificationDefinition, UserNotificationDeliveryType}

class ActivityNotificationDefinition
  extends UserNotificationDefinition(
      "com_arcusys_valamis_web_portlet_ValamisActivitiesView",
      0L,
      0,
      "likes or comments your activity.") { //TODO: add localization

      this.addUserNotificationDeliveryType(new UserNotificationDeliveryType("website", 10002, true, true))

  }
