package com.arcusys.valamis.grade.service

import com.arcusys.learn.liferay.services.SocialActivityLocalServiceHelper
import com.arcusys.valamis.grade.model.CourseGrade
import com.arcusys.valamis.grade.storage.CourseGradeStorage
import com.arcusys.valamis.lesson._
import com.arcusys.valamis.lesson.model.CourseActivityType
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CourseGradeServiceImpl(implicit val bindingModule: BindingModule) extends CourseGradeService with Injectable {

  val courseRepository = inject[CourseGradeStorage]

  def get(courseId: Long, userId: Long) = {
    courseRepository.get(courseId, userId)
  }

  def set(courseId: Long, userId: Long, grade: Option[Float], comment: Option[String], companyId: Long) {
    courseRepository.get(courseId, userId) match {
      case Some(course) =>
        courseRepository.modify(course.copy(
          grade = grade,
          comment = comment.getOrElse("")
        ))
      case None =>
        courseRepository.create(CourseGrade(
          courseId,
          userId,
          grade,
          comment.getOrElse(""),
          date = None
        ))
    }

    if (grade.exists(_ > LessonSuccessLimit)) {
      SocialActivityLocalServiceHelper.addWithSet(
        companyId,
        userId,
        CourseActivityType.getClass.getName,
        courseId = Option(courseId.toLong),
        `type` = Some(CourseActivityType.Completed.id),
        classPK = Option(courseId)
      )
    }
  }
}
