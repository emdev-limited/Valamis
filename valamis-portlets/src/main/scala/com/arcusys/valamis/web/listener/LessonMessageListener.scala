package com.arcusys.valamis.web.listener

import java.io.File

import com.arcusys.learn.liferay.LiferayClasses.{LMessage, LMessageListener}
import com.arcusys.learn.liferay.services.{CompanyHelper, GroupLocalServiceHelper, MessageBusHelper}
import com.arcusys.valamis.gradebook.service.{LessonGradeService, LessonSuccessLimit}
import com.arcusys.valamis.lesson.model.{Lesson, PackageActivityType}
import com.arcusys.valamis.lesson.service.{LessonAssetHelper, LessonNotificationService, LessonService, TeacherLessonGradeService}
import com.arcusys.valamis.lesson.tincan.service.TincanPackageUploader
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

//JSONFactoryUtil is in the same package for both LR, so use it directly
import com.liferay.portal.kernel.json.JSONFactoryUtil


/**
  * Created by pkornilov on 3/16/17.
  */
class LessonMessageListener extends LMessageListener
  with Injectable
  with LogSupport
  with MessageExtensions {
  val bindingModule: BindingModule = Configuration

  private lazy val lessonService = inject[LessonService]
  private lazy val gradeService = inject[LessonGradeService]
  private lazy val teacherGradeService = inject[TeacherLessonGradeService]
  private lazy val lessonAssetHelper = inject[LessonAssetHelper]
  private lazy val uploader = inject[TincanPackageUploader]
  private lazy val lessonSocialActivityHelper = new SocialActivityHelper[Lesson]
  private lazy val lessonNotificationService = inject[LessonNotificationService]

  override def receive(message: LMessage): Unit = {
    val responseMessage = MessageBusHelper.createResponseMessage(message)

    try {
      val action = message.getStringRequired(LessonMessageFields.Action)

      val payload = {
        val actionType = try {
          LessonActions.withName(action)
        } catch {
          case _: NoSuchElementException =>
            throw new NoSuchMethodException(s"Action $action is not supported")
        }

        actionType match {
          case LessonActions.IsDeployed => "deployed"
          case LessonActions.GetLessonNames => getLessonNames(message)
          case LessonActions.GetLessonStatus => getLessonStatus(message)
          case LessonActions.UploadPackage => uploadPackage(message)
          case _ => throw new NoSuchMethodException(s"Action $actionType is not supported")
        }
      }

      responseMessage.setPayload(payload)
    } catch {
      case ex: Throwable =>
        log.error(s"Failed to process message $message", ex)
        responseMessage.setPayload(
          s""" {
             |  "${LessonJsonFields.Error}": "${ex.getMessage}"
             | }
         """.stripMargin
        )
    }

    MessageBusHelper.sendAsynchronousMessage(message.getResponseDestinationName, responseMessage)
  }

  private def getLessonNames(message: LMessage): String = {
    val ids = message.getStringRequired(LessonMessageFields.LessonIds).split(',').map(_.toLong)

    val jsonObject = JSONFactoryUtil.createJSONObject()
    val lessonJsonArray = JSONFactoryUtil.createJSONArray()


    lessonService.getLessonTitlesByIds(ids) foreach { case (id, title) =>
      val lJsonObject = JSONFactoryUtil.createJSONObject()

      lJsonObject.put(LessonJsonFields.Id, id)
      lJsonObject.put(LessonJsonFields.Title, title)

      lessonJsonArray.put(lJsonObject)
    }

    jsonObject.put(LessonJsonFields.Lessons, lessonJsonArray)
    jsonObject.toString()
  }

  private def getLessonStatus(message: LMessage): String = {
    val lessonId = message.getLongRequired(LessonMessageFields.LessonId)
    val userId = message.getLongRequired(LessonMessageFields.UserId)
    val companyId = message.getLongRequired(LessonMessageFields.CompanyId)

    val jsonObject = JSONFactoryUtil.createJSONObject()

    val isCompleted = lessonService.getLesson(lessonId) match {
      case Some(lesson) =>
        CompanyHelper.setCompanyId(companyId)//need for lrs reader to work
        //TODO resolve it another way
        val grade = teacherGradeService.get(userId, lessonId).flatMap(_.grade)
        gradeService.isLessonFinished(grade, userId, lesson)
      case None => false
    }

    jsonObject.put(LessonJsonFields.IsCompleted, isCompleted)

    jsonObject.toString

  }

  private def uploadPackage(message: LMessage): String = {
    val courseId = message.getLongRequired("courseId")
    val userId = message.getLongRequired(LessonMessageFields.UserId)

    val title = message.getStringRequired("title")
    val description = message.getStringRequired("description")

    val packageFilePath = message.getStringRequired("package")

    val scoreLimit = message.getDoubleOptional("scoreLimit").getOrElse(LessonSuccessLimit)
    val requiredReview = message.getBoolean("requiredReview")

    //TODO: add lesson goals for story tree  //packageCategoryGoalStorage.add(packageGoals)
    //TODO: add tags //lessonTagService.setTags(packageAssetId, tagsIds)

    val (lesson, oldLessonId) = uploader.updateLesson(
      title,
      description,
      new File(packageFilePath),
      courseId,
      userId,
      scoreLimit,
      requiredReview
    )

    val packageAssetId = lessonAssetHelper.updatePackageAssetEntry(lesson)
    lessonSocialActivityHelper.addWithSet(
      GroupLocalServiceHelper.getGroup(lesson.courseId).getCompanyId,
      lesson.ownerId,
      courseId = Some(lesson.courseId),
      `type` = Some(PackageActivityType.Published.id),
      classPK = Some(lesson.id),
      createDate = DateTime.now)

    if (oldLessonId.isEmpty) {
      lessonNotificationService.sendLessonAvailableNotification(Seq(lesson),lesson.courseId)
    }

    s"""{"lessonId":${lesson.id} }"""
  }
}

object LessonActions extends Enumeration {
  val IsDeployed = Value("Check")
  val GetLessonStatus = Value("LessonStatus")
  val GetLessonNames = Value("LessonNames")
  /** used in Lesson Studio to publish lesson **/
  val UploadPackage = Value("UploadPackage")
}

object LessonJsonFields {
  val IsCompleted = "isCompleted"
  val Date = "date"
  val Lessons = "lessons"
  val Id = "id"
  val Title = "title"
  val Error = "error"
}

object LessonMessageFields {
  val Action = "action"
  val LessonId = "lessonId"
  val LessonIds = "lessonIds"
  val UserId = "userId"
  val CompanyId = "companyId"
}
