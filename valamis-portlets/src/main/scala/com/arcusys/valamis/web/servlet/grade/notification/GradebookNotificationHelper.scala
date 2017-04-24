package com.arcusys.valamis.web.servlet.grade.notification

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName, ServiceContextFactoryHelper, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.util.serialization.JsonHelper
import org.joda.time.DateTime
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.service.LessonService
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.kernel.util.PrefsPropsUtil

abstract class GradebookNotificationHelper{

  def lessonService: LessonService

  val NotificationType = PortletName.Gradebook.key

  private val log: Log = LogFactoryUtil.getLog(this.getClass)

  def sendTotalGradeNotification (courseId: Long,
                        userId: Long,
                        studentId: Long,
                        grade: Float,
                        httpRequest: HttpServletRequest ): Unit = {

    try {
      val course = GroupLocalServiceHelper.getGroup(courseId)
      val gradePercent = (grade * 100).toInt.toString
      val context = ServiceContextHelper.getServiceContext
      val user = UserLocalServiceHelper().getUser(studentId)
      val company = CompanyLocalServiceHelper.getCompany(course.getCompanyId)
      if (PrefsPropsUtil.getBoolean(company.getCompanyId, "valamis.grade.lesson.enable")) {
        EmailNotificationHelper.sendNotification(course.getCompanyId,
          studentId,
          "valamisGradeCourseBody",
          "valamisGradeCourseSubject",
          Map(
            "[$COURSE_LINK$]" -> getLink(PortalUtilHelper.getPortalURL(
              context.getRequest).concat(course.getCourseFriendlyUrl),
              course.getDescriptiveName),
            "[$GRADE$]" -> gradePercent,
            "[$USER_SCREENNAME$]" -> user.getFullName,
            "[$PORTAL_URL$]" -> company.getVirtualHostname
          )
        )
      }

      val notification = GradebookNotificationModel(
        "grade",
        courseId,
        userId,
        (grade * 100).toInt.toString // *100 because grade is sent in range between 0 and 1
      )

      sendNotification(userId, studentId, notification, httpRequest)
    } catch {
      case e: Exception => log.error(e)
    }
  }

  def sendPackageGradeNotification (courseId: Long,
                                    userId: Long,
                                    studentId: Long,
                                    grade: Float,
                                    lesson: Lesson,
                                    httpRequest: HttpServletRequest) : Unit = {

    try {
      val course = GroupLocalServiceHelper.getGroup(courseId)
      val gradePercent = (grade * 100).toInt.toString
      val user = UserLocalServiceHelper().getUser(studentId)
      val company = CompanyLocalServiceHelper.getCompany(course.getCompanyId)
      getLink(lessonService.getLessonURL(lesson, company.getCompanyId), lesson.title)
      if (PrefsPropsUtil.getBoolean(company.getCompanyId, "valamis.grade.lesson.enable")) {

        EmailNotificationHelper.sendNotification(course.getCompanyId,
          studentId,
          "valamisGradeLessonBody",
          "valamisGradeLessonSubject",
          Map(
            "[$LESSON_LINK$]" -> getLink(lessonService.getLessonURL(lesson,
              company.getCompanyId), lesson.title),
            "[$GRADE$]" -> gradePercent,
            "[$USER_SCREENNAME$]" -> user.getFullName,
            "[$PORTAL_URL$]" -> company.getVirtualHostname
          )
        )
      }
      val notification = GradebookNotificationModel(
        "package_grade",
        courseId,
        userId,
        gradePercent, // *100 because grade is sent in range between 0 and 1
        lesson.title
      )

      sendNotification(userId, studentId, notification, httpRequest)
    } catch {
      case e: Exception => log.error(e)
    }
  }

  def sendStatementCommentNotification (courseId: Long,
                                   userId: Long,
                                   targetId: Long,
                                   packageTitle: String,
                                   httpRequest: HttpServletRequest): Unit = {

    val notification = GradebookNotificationModel (
      "statement_comment",
      courseId,
      userId,
      packageTitle = packageTitle
    )

    sendNotification(userId, targetId, notification, httpRequest)
  }

  private def sendNotification (userId: Long,
                                targetId: Long,
                                model: GradebookNotificationModel,
                                request: HttpServletRequest ): Unit = {
    //If sender user is not the same as student
    if(userId != targetId) {
      //Sending notification to student
      UserNotificationEventLocalServiceHelper.addUserNotificationEvent(
        targetId,
        NotificationType,
        DateTime.now().getMillis,
        targetId,
        JsonHelper.toJson(model),
        false,
        ServiceContextFactoryHelper.getInstance(request)
      )
    }
  }

  private def getLink(link: String, name: String): String = s"""<a href="$link">$name</a>"""
}
