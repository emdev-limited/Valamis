package com.arcusys.valamis.persistence.impl.course

import com.arcusys.valamis.course.storage.{CourseInstructorRepository}
import com.arcusys.valamis.course.model.CourseInstructor
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.course.schema.CourseInstructorTableComponent
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

class CourseInstructorRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CourseInstructorRepository
    with CourseInstructorTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  override def getByCourseId(id: Long): Seq[CourseInstructor] = execSync {
    courseInstructors.filter(_.courseId === id).result
  }

  override def update(courseId: Long, instructorIds: Seq[Long], modifiedDate: DateTime): Unit = {
    val deleteOldRows = courseInstructors.filter {
      _.courseId === courseId
    } filterNot {
      _.instructorId inSet instructorIds
    } delete

    val insertNewRows = courseInstructors ++= instructorIds.filter {
      !isExist(courseId, _)
    } map {
      CourseInstructor(courseId, _, modifiedDate)
    }
    execSyncInTransaction(deleteOldRows >> insertNewRows)
  }

  override def isExist(courseId: Long, instructorId: Long): Boolean = execSync {
    courseInstructors.filter {
      _.courseId === courseId
    }.filter {
      _.instructorId === instructorId
    }.exists.result
  }

  override def deleteInstructors(courseId: Long): Unit = execSync {
    courseInstructors.filter {
      _.courseId === courseId
    } delete
  }
}