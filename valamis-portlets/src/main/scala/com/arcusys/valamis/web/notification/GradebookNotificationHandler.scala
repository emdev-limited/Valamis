package com.arcusys.valamis.web.notification

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.{CourseUtilHelper, LanguageHelper, PortletName}
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.grade.notification.GradebookNotificationModel

class GradebookNotificationHandler extends LBaseUserNotificationHandler {
  setPortletId(PortletName.Gradebook.key)

  override protected def getLink(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[GradebookNotificationModel](userNotificationEvent.getPayload)

    CourseUtilHelper.getLink(notification.courseId)
  }

  override protected def getBody(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[GradebookNotificationModel](userNotificationEvent.getPayload)

    val userLocale = UserLocalServiceHelper().getUser(notification.userId).getLocale
    val tpl = LanguageHelper.get(userLocale, s"gradebook.${notification.messageType}")

    val mustached = new Mustache(tpl)
    mustached.render(getParams(notification))
  }

  private def getParams (notification: GradebookNotificationModel) = {
    val userName = UserLocalServiceHelper().getUser(notification.userId).getFullName
    val courseName = CourseUtilHelper.getName(notification.courseId).getOrElse("")

    Map (
      "user" -> userName,
      "course" -> courseName,
      "grade" -> notification.grade,
      "package" -> notification.packageTitle
    )
  }
}
