package com.arcusys.learn.scorm.course.impl.liferay

import com.arcusys.learn.persistence.liferay.model.LFCourse
import com.arcusys.learn.persistence.liferay.service.LFCourseLocalServiceUtil
import com.arcusys.valamis.grade.model.CourseGrade
import com.arcusys.valamis.grade.storage.CourseGradeStorage
import org.joda.time.DateTime

import scala.util.Try

// TODO convert grade column type to float
class CourseRepositoryImpl extends CourseGradeStorage {
  
  override def get(courseId: Long, userId: Long): Option[CourseGrade] = {
    Option(LFCourseLocalServiceUtil.fetchByCourseIdAndUserId(courseId.toInt, userId.toInt)) map extract
  }

  override def create(course: CourseGrade): Unit = {
    val lfEntity = LFCourseLocalServiceUtil.createLFCourse()

    lfEntity.setCourseID(course.courseId.toInt)
    lfEntity.setUserID(course.userId.toInt)
    lfEntity.setGrade(course.grade.map(_.toString).getOrElse(""))
    lfEntity.setComment(course.comment)
    lfEntity.setDate(DateTime.now().toDate)

    LFCourseLocalServiceUtil.addLFCourse(lfEntity)
  }

  override def modify(course: CourseGrade): Unit = {
    val lfEntity = LFCourseLocalServiceUtil
      .findByCourseIdAndUserId(course.courseId.toInt, course.userId.toInt)

    lfEntity.setCourseID(course.courseId.toInt)
    lfEntity.setUserID(course.userId.toInt)
    lfEntity.setGrade(course.grade.map(_.toString).getOrElse(""))
    lfEntity.setComment(course.comment)
    lfEntity.setDate(DateTime.now().toDate)

    LFCourseLocalServiceUtil.updateLFCourse(lfEntity)
  }

  private def extract(lfEntity: LFCourse) = CourseGrade(
    lfEntity.getCourseID.toLong,
    lfEntity.getUserID.toLong,
    Try(lfEntity.getGrade.toFloat).toOption,
    lfEntity.getComment,
    Option(lfEntity.getDate).map(d => new DateTime(d))
  )
}
