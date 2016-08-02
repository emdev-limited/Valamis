package com.arcusys.valamis.web.servlet.grade.notification

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.util.{PortletName, ServiceContextFactoryHelper, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.util.serialization.JsonHelper
import org.joda.time.DateTime

object GradebookNotificationHelper {

  val NotificationType = PortletName.Gradebook.key

  def sendTotalGradeNotification (courseId: Long,
                        userId: Long,
                        studentId: Long,
                        grade: Option[Float],
                        httpRequest: HttpServletRequest ): Unit = {

    val notification = GradebookNotificationModel (
      "grade",
      courseId,
      userId,
      grade.map(_*100).map(_.toInt.toString).getOrElse("0") // *100 because grade is sent in range between 0 and 1
    )

    sendNotification(userId, studentId, notification, httpRequest)
  }

  def sendPackageGradeNotification (courseId: Long,
                                    userId: Long,
                                    studentId: Long,
                                    grade: Option[Float],
                                    packageTitle: String,
                                    httpRequest: HttpServletRequest) : Unit = {

    val notification = GradebookNotificationModel (
      "package_grade",
      courseId,
      userId,
      grade.map(_*100).map(_.toInt.toString).getOrElse("0"), // *100 because grade is sent in range between 0 and 1
      packageTitle
    )

    sendNotification(userId, studentId, notification, httpRequest)
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
}
