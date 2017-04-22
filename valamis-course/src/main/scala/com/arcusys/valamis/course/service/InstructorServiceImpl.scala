package com.arcusys.valamis.course.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.course.storage.CourseInstructorRepository
import org.joda.time.DateTime

trait InstructorServiceImpl extends InstructorService {

  def instructorRepository: CourseInstructorRepository

  override def getByCourseId(courseId: Long): Seq[LUser] = {
    instructorRepository.getByCourseId(courseId)
      .map(row => UserLocalServiceHelper().fetchUser(row.instructorId))
      .flatten
  }

  override def updateInstructors(courseId: Long, instructorIds: Seq[Long]): Unit = {
    if (instructorIds.isEmpty) {
      instructorRepository.deleteInstructors(courseId)
    } else {
      instructorRepository.update(courseId, instructorIds, DateTime.now)
    }
  }

  override def isExist(courseId: Long, instructorId: Long): Boolean = {
    instructorRepository.isExist(courseId, instructorId)
  }

  override def deleteInstructors(courseId: Long): Unit = {
    instructorRepository.deleteInstructors(courseId)
  }
}