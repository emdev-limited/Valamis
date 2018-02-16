package com.arcusys.valamis.course.storage

import com.arcusys.valamis.course.model.CourseInstructor
import org.joda.time.DateTime

trait CourseInstructorRepository {

  def getByCourseId(id: Long): Seq[CourseInstructor]

  def update(courseId: Long, instructorIds: Seq[Long], modifiedDate: DateTime): Unit

  def isExist(courseId: Long, instructorId: Long): Boolean

  def deleteInstructors(courseId: Long): Unit
}