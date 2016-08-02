package com.arcusys.learn.liferay.util

import com.arcusys.learn.liferay.LiferayClasses.LServiceContext
import com.liferay.portal.service.UserNotificationEventLocalServiceUtil

object UserNotificationEventLocalServiceHelper {

  def addUserNotificationEvent(userId: Long,
                               activityType: String,
                               timestamp: Long,
                               deliverBy: Long,
                               payload: String,
                               archived: Boolean,
                               serviceContext: LServiceContext
                              ) = {
    UserNotificationEventLocalServiceUtil.addUserNotificationEvent(
      userId,
      activityType,
      timestamp,
      deliverBy,
      payload,
      archived,
      serviceContext
    )
  }
}
