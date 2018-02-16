package com.arcusys.valamis.course.service

import com.arcusys.learn.liferay.LiferayClasses.LUser

trait InstructorService {

  def getByCourseId(courseId: Long): Seq[LUser]

  def updateInstructors(courseId: Long, instructorIds: Seq[Long]): Unit

  def isExist(courseId: Long, instructorId: Long): Boolean

  def deleteInstructors(courseId: Long): Unit
}