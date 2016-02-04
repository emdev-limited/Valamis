package com.arcusys.valamis.course

trait UserCourseResultService {

  def set(courseId: Long, userId: Long, isCompleted: Boolean)

  def isCompleted(courseId: Long, userId: Long, packagesCount: Option[Long] = None): Boolean

  def setCourseNotCompleted(courseId: Long): Unit

  def resetCourseResults(courseId: Long): Unit
}
