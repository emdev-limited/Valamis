package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.{CourseUtilHelper, PortalUtilHelper, PortletName, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.lesson.model.{Lesson, LessonNotificationModel}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.kernel.util.PrefsPropsUtil


abstract class LessonNotificationServiceImpl extends LessonNotificationService {
  private val log: Log = LogFactoryUtil.getLog(this.getClass)

  def lessonService: LessonService


  def sendLessonAvailableNotification(lessons: Seq[Lesson], courseId: Long): Unit = {
    try {

      val course = GroupLocalServiceHelper.getGroup(courseId)

      val company = CompanyLocalServiceHelper.getCompany(course.getCompanyId)

      lessons.foreach { lesson =>
        val link = lessonService.getLessonURL(lesson, company.getCompanyId)

        val linkHtml = getLink(link, lesson.title)


        val users = UserLocalServiceHelper().getGroupUsers(courseId)
        users.foreach { user =>
          val userId = user.getUserId
          if (PrefsPropsUtil.getBoolean(course.getCompanyId, "valamis.course.lesson.available.enable")) {

            EmailNotificationHelper.sendNotification(company.getCompanyId,
              userId,
              "valamisCourseLessonAvailableBody",
              "valamisCourseLessonAvailableSubject",
              Map(
                "[$USER_SCREENNAME$]" -> user.getFullName,
                "[$LESSON_LINK$]" -> linkHtml,
                "[$PORTAL_URL$]" -> company.getVirtualHostname,
                "[$COURSE_NAME$]" -> course.getName,
                "[$COURSE_LINK$]" -> getLink(PortalUtilHelper.getLocalHostUrlForCompany(company.getCompanyId)
                  .concat(CourseUtilHelper.getLink(courseId)), course.getDescriptiveName)
              )
            )
          }
          prepareSendNotification(userId, "available", lesson.title, link, courseId)
        }
      }
    } catch {
      case e: Exception => log.error(e)
    }
  }


  private def prepareSendNotification(userId: Long,
                                      messageType: String,
                                      title: String,
                                      link: String,
                                      courseId: Long): Unit = {
    val model = LessonNotificationModel(messageType, title, link, courseId, userId)
    UserNotificationEventLocalServiceHelper.sendNotification(JsonHelper.toJson(model),
      userId,
      PortletName.LessonViewer.key)
  }

  private def getLink(link: String, name: String): String = "<a href=\"" + link + "\">" + name + "</a>"
}
