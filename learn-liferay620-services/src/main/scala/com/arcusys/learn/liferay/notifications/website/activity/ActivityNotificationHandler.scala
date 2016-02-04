package com.arcusys.learn.liferay.notifications.website.activity

import com.arcusys.learn.liferay.model.ActivityNotificationModel
import com.arcusys.learn.liferay.notifications.website.NotificationType
import com.arcusys.learn.liferay.util.CourseUtilHelper
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.kernel.language.LanguageUtil
import com.liferay.portal.kernel.notifications.BaseUserNotificationHandler
import com.liferay.portal.model.UserNotificationEvent
import com.liferay.portal.service.{ServiceContext, UserLocalServiceUtil}


class ActivityNotificationHandler extends BaseUserNotificationHandler {
  setPortletId(NotificationType.Activity)

  override protected def getLink(userNotificationEvent: UserNotificationEvent, serviceContext: ServiceContext) = {
    val notification = JsonHelper.fromJson[ActivityNotificationModel](userNotificationEvent.getPayload)

    CourseUtilHelper.getLink(notification.courseId)
  }

  override protected def getBody(userNotificationEvent: UserNotificationEvent, serviceContext: ServiceContext) = {
    val notification = JsonHelper.fromJson[ActivityNotificationModel](userNotificationEvent.getPayload)

    val userLocale = UserLocalServiceUtil.getUser(notification.userId).getLocale
    val tpl = LanguageUtil.get(userLocale, s"activity.${notification.messageType}")

    val mustached = new Mustache(tpl)
    mustached.render(getParams(notification))
  }

  private def getParams (notification: ActivityNotificationModel) = {
    val userName = UserLocalServiceUtil.getUser(notification.userId).getFullName
    val courseName = CourseUtilHelper.getName(notification.courseId)

    Map (
      "user" -> userName,
      "course" -> courseName
    )
  }
}
