package com.arcusys.valamis.course.storage

import com.arcusys.valamis.course.model.UserCourseResult

trait UserCourseResultStorage {
  def get(courseId: Long, userId: Long): Option[UserCourseResult]

  def getByUserId(userId: Long): Seq[UserCourseResult]

  private[course] def insertOrUpdate(userCourse: UserCourseResult): Unit

  private[course] def update(courseId: Long, isCompleted: Boolean): Unit

  private[course] def delete(courseId: Long): Unit
}
