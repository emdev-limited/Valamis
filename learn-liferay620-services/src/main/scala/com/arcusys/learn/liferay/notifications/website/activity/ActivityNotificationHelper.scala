package com.arcusys.learn.liferay.notifications.website.activity

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.model.{Activity, ActivityNotificationModel}
import com.arcusys.learn.liferay.notifications.MessageType.MessageType
import com.arcusys.learn.liferay.notifications.website.NotificationType
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.service.{ServiceContextFactory, UserNotificationEventLocalServiceUtil}
import org.joda.time.DateTime

object ActivityNotificationHelper {

  def sendNotification (messageType: MessageType,
                        courseId: Long,
                        userId: Long,
                        httpRequest: HttpServletRequest,
                        activity: Activity   ): Unit = {

    val notification = ActivityNotificationModel (
      messageType.toString,
      courseId,
      userId
    )

    //If sender user is not the same as activity creator
    if(userId != activity.userId)
      //Sending notification to activity creator
      UserNotificationEventLocalServiceUtil.addUserNotificationEvent(
        activity.userId,
        NotificationType.Activity,
        DateTime.now().getMillis,
        activity.userId,
        JsonHelper.toJson(notification),
        false,
        ServiceContextFactory.getInstance(httpRequest)
      )
  }
}
