package com.arcusys.valamis.gradebook.service

import com.arcusys.learn.liferay.LiferayClasses.LUser

trait UserCourseResultService {

  def set(courseId: Long, userId: Long, isCompleted: Boolean): Unit

  def isCompleted(courseId: Long, user: LUser, packagesCount: Option[Long] = None): Boolean

  def setCourseNotCompleted(courseId: Long): Unit

  def resetCourseResults(courseId: Long): Unit
}
