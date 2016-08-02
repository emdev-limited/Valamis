package com.arcusys.valamis.web.servlet.social.notification

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.model.Activity
import com.arcusys.learn.liferay.util.{PortletName, ServiceContextFactoryHelper, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.util.serialization.JsonHelper
import org.joda.time.DateTime

object ActivityNotificationHelper {

  val NotificationType = PortletName.ValamisActivities.key

  def sendCommentNotification(courseId: Long,
                              userId: Long,
                              httpRequest: HttpServletRequest,
                              activity: Activity): Unit = {
    sendNotification("comment", courseId, userId, httpRequest, activity)
  }

  def sendLikeNotification(courseId: Long,
                           userId: Long,
                           httpRequest: HttpServletRequest,
                           activity: Activity): Unit = {
    sendNotification("like", courseId, userId, httpRequest, activity)
  }

  private def sendNotification(messageType: String,
                        courseId: Long,
                        userId: Long,
                        httpRequest: HttpServletRequest,
                        activity: Activity): Unit = {

    val notification = ActivityNotificationModel (
      messageType,
      courseId,
      userId
    )

    //If sender user is not the same as activity creator
    if(userId != activity.userId) {
      //Sending notification to activity creator
      UserNotificationEventLocalServiceHelper.addUserNotificationEvent(
        activity.userId,
        NotificationType,
        DateTime.now().getMillis,
        activity.userId,
        JsonHelper.toJson(notification),
        false,
        ServiceContextFactoryHelper.getInstance(httpRequest)
      )
    }
  }
}
