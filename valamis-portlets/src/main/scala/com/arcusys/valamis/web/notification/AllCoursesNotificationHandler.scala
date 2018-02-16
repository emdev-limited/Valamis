package com.arcusys.valamis.web.notification

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.{LanguageHelper, PortletName}
import com.arcusys.valamis.model.{AllCoursesNotificationModel, CourseNotificationModel}
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper


class AllCoursesNotificationHandler extends LBaseUserNotificationHandler {
  setPortletId(PortletName.AllCourses.key)

  override protected def getLink(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[AllCoursesNotificationModel](userNotificationEvent.getPayload)
    notification.courseLink
  }

  override protected def getBody(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[AllCoursesNotificationModel](userNotificationEvent.getPayload)

    val userLocale = UserLocalServiceHelper().getUser(notification.userId).getLocale
    val tpl = LanguageHelper.get(userLocale, s"allcourses.${notification.messageType}")

    val mustached = new Mustache(tpl)
    mustached.render(getParams(notification))
  }

  private def getParams(notification: AllCoursesNotificationModel) = {
    Map("user" -> notification.trigUserName,
      "title" -> notification.courseTitle)
  }
}