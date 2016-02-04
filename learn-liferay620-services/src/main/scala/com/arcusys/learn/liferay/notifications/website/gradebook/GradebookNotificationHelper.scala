package com.arcusys.learn.liferay.notifications.website.gradebook

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.model.GradebookNotificationModel
import com.arcusys.learn.liferay.notifications.MessageType
import com.arcusys.learn.liferay.notifications.website.NotificationType
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.service.{ServiceContextFactory, UserNotificationEventLocalServiceUtil}
import org.joda.time.DateTime

object GradebookNotificationHelper {

  def sendTotalGradeNotification (courseId: Long,
                        userId: Long,
                        studentId: Long,
                        grade: Option[Float],
                        httpRequest: HttpServletRequest ): Unit = {

    val notification = GradebookNotificationModel (
      MessageType.Grade.toString,
      courseId,
      userId,
      grade.map(_.toInt.toString).getOrElse("0")
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
      MessageType.PackageGrade.toString,
      courseId,
      userId,
      grade.map(_.toInt.toString).getOrElse("0"),
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
      MessageType.StatementComment.toString,
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
    if(userId != targetId)
    //Sending notification to student
      UserNotificationEventLocalServiceUtil.addUserNotificationEvent(
        targetId,
        NotificationType.Gradebook,
        DateTime.now().getMillis,
        targetId,
        JsonHelper.toJson(model),
        false,
        ServiceContextFactory.getInstance(request)
      )
  }
}
