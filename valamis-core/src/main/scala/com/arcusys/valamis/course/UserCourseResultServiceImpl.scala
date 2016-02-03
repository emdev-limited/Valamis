package com.arcusys.valamis.course

import com.arcusys.valamis.course.model.UserCourseResult
import com.arcusys.valamis.course.storage.UserCourseResultStorage
import com.arcusys.valamis.lesson.PackageChecker
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class UserCourseResultServiceImpl(implicit val bindingModule: BindingModule)
  extends UserCourseResultService
  with Injectable {

  private lazy val courseStorage = inject[UserCourseResultStorage]
  private lazy val packageChecker = inject[PackageChecker]

  override def isCompleted(courseId: Long, userId: Long, packagesCount: Option[Long]): Boolean = {
    courseStorage.get(courseId, userId).map(_.isCompleted) match {
      case Some(isCompleted) => isCompleted
      case None =>
        val isCompleted = packageChecker.isCourseCompleted(courseId, userId)

        set(courseId, userId, isCompleted)

        isCompleted
    }
  }

  override def set(courseId: Long, userId: Long, isCompleted: Boolean) = {
    courseStorage.insertOrUpdate(new UserCourseResult(courseId, userId, isCompleted))
  }

  override def setCourseNotCompleted(courseId: Long) {
    courseStorage.update(courseId, isCompleted = false)
  }

  override def resetCourseResults(courseId: Long) {
    courseStorage.delete(courseId)
  }
}
