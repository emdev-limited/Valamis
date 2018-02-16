package com.arcusys.valamis.gradebook.service

trait UserCourseResultService {

  def set(courseId: Long, userId: Long, isCompleted: Boolean): Unit

  def getCompletedCount(courseId: Long, userIds: Seq[Long]): Int

  def setCourseNotCompleted(courseId: Long): Unit

  def resetCourseResults(courseId: Long): Unit
}
