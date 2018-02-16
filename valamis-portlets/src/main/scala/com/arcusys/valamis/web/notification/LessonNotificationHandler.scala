package com.arcusys.valamis.web.notification

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.{CourseUtilHelper, LanguageHelper, PortletName}
import com.arcusys.valamis.lesson.model.LessonNotificationModel
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper


class LessonNotificationHandler extends LBaseUserNotificationHandler {
  setPortletId(PortletName.LessonViewer.key)

  override protected def getLink(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[LessonNotificationModel](userNotificationEvent.getPayload)
    notification.lessonLink
  }

  override protected def getBody(userNotificationEvent: LUserNotificationEvent, serviceContext: LServiceContext) = {
    val notification = JsonHelper.fromJson[LessonNotificationModel](userNotificationEvent.getPayload)

    val userLocale = UserLocalServiceHelper().getUser(notification.userId).getLocale
    val tpl = LanguageHelper.get(userLocale, s"lesson.${notification.messageType}")

    val mustached = new Mustache(tpl)
    mustached.render(getParams(notification))
  }

  private def getParams(notification: LessonNotificationModel) = {
    Map("title" -> notification.title,
        "course" -> CourseUtilHelper.getName(notification.courseId).getOrElse(""))
  }
}