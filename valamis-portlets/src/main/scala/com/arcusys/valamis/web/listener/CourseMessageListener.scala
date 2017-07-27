package com.arcusys.valamis.web.listener

import com.arcusys.learn.liferay.LiferayClasses.{LMessage, LMessageListener}
import com.arcusys.learn.liferay.services.MessageBusHelper
import com.arcusys.valamis.gradebook.service.TeacherCourseGradeService
import com.arcusys.valamis.gradebook.service.LessonSuccessLimit
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

//JSONFactoryUtil is in the same package for both LR, so use it directly
import com.liferay.portal.kernel.json.JSONFactoryUtil


/**
  * Created by pkornilov on 3/16/17.
  */
class CourseMessageListener extends LMessageListener
  with Injectable
  with LogSupport
  with MessageExtensions {
  val bindingModule: BindingModule = Configuration

  private lazy val courseGradeStorage = inject[TeacherCourseGradeService]

  //it's not thread safe, so use def
  private def dtFormatter = ISODateTimeFormat.dateTimeNoMillis()

  override def receive(message: LMessage): Unit = {
    val responseMessage = MessageBusHelper.createResponseMessage(message)

    try {
      val action = message.getStringRequired(CourseMessageFields.Action)

      val payload = {
        val actionType = try {
          CourseActionType.withName(action)
        } catch {
          case _: NoSuchElementException =>
            throw new NoSuchMethodException(s"Action $action is not supported")
        }

        actionType match {
          case CourseActionType.Check => "deployed"
          case CourseActionType.CourseStatus => getCourseStatus(message)
          case _ => throw new NoSuchMethodException(s"Action $actionType is not supported")
        }
      }

      responseMessage.setPayload(payload)
    } catch {
      case ex: Throwable =>
        log.error(s"Failed to process message $message", ex)
        responseMessage.setPayload(
          s""" {
             |  "${CourseJsonFields.Error}": "${ex.getMessage}"
             | }
         """.stripMargin
        )
    }

    MessageBusHelper.sendAsynchronousMessage(message.getResponseDestinationName, responseMessage)
  }

  private def getCourseStatus(message: LMessage): String = {
    val courseId = message.getLongRequired(CourseMessageFields.CourseId)
    val userId = message.getLongRequired(CourseMessageFields.UserId)

    val jsonObject = JSONFactoryUtil.createJSONObject()

    val (isCompleted, date) = courseGradeStorage.get(courseId, userId) match {
      case Some(courseGrade) => (courseGrade.grade.exists(_ > LessonSuccessLimit), courseGrade.date)
      case None => (false, DateTime.now())
    }

    jsonObject.put(CourseJsonFields.IsCompleted, isCompleted)
    jsonObject.put(CourseJsonFields.Date, date.toString(dtFormatter))

    jsonObject.toString

  }

}

object CourseActionType extends Enumeration {
  val CourseStatus = Value
  val Check = Value
}

object CourseJsonFields {
  val IsCompleted = "isCompleted"
  val Date = "date"
  val Id = "id"
  val Error = "error"
}

object CourseMessageFields {
  val Action = "action"
  val CourseId = "courseId"
  val UserId = "userId"
  val CompanyId = "companyId"
}
