package com.arcusys.learn.liferay.notifications.website.gradebook

import com.arcusys.learn.liferay.model.GradebookNotificationModel
import com.arcusys.learn.liferay.notifications.website.NotificationType
import com.arcusys.learn.liferay.util.CourseUtilHelper
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.kernel.language.LanguageUtil
import com.liferay.portal.kernel.notifications.BaseUserNotificationHandler
import com.liferay.portal.model.UserNotificationEvent
import com.liferay.portal.service.{ServiceContext, UserLocalServiceUtil}


class GradebookNotificationHandler extends BaseUserNotificationHandler {
  setPortletId(NotificationType.Gradebook)

  override protected def getLink(userNotificationEvent: UserNotificationEvent, serviceContext: ServiceContext) = {
    val notification = JsonHelper.fromJson[GradebookNotificationModel](userNotificationEvent.getPayload)

    CourseUtilHelper.getLink(notification.courseId)
  }

  override protected def getBody(userNotificationEvent: UserNotificationEvent, serviceContext: ServiceContext) = {
    val notification = JsonHelper.fromJson[GradebookNotificationModel](userNotificationEvent.getPayload)

    val userLocale = UserLocalServiceUtil.getUser(notification.userId).getLocale
    val tpl = LanguageUtil.get(userLocale, s"gradebook.${notification.messageType}")

    val mustached = new Mustache(tpl)
    mustached.render(getParams(notification))
  }

  private def getParams (notification: GradebookNotificationModel) = {
    val userName = UserLocalServiceUtil.getUser(notification.userId).getFullName
    val courseName = CourseUtilHelper.getName(notification.courseId)

    Map (
      "user" -> userName,
      "course" -> courseName,
      "grade" -> notification.grade,
      "package" -> notification.packageTitle
    )
  }
}
