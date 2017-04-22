package com.arcusys.valamis.persistence.impl.course

import com.arcusys.valamis.course.model.CourseExtended
import com.arcusys.valamis.course.storage.CourseExtendedRepository
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model.SlideSet
import com.arcusys.valamis.slide.storage.SlideSetRepository
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.course.schema.CourseExtendedTableComponent
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import scala.concurrent.duration.Duration
import scala.concurrent.Await


class CourseExtendedRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CourseExtendedRepository
    with CourseExtendedTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  override def getById(id: Long): Option[CourseExtended] = execSync {
    coursesExtended.filter(_.courseId === id).result.headOption
  }

  override def delete(id: Long): Unit = execSync {
    coursesExtended.filter(_.courseId === id).delete
  }

  override def create(courseExtended: CourseExtended): CourseExtended = {
    execSync {
      coursesExtended += courseExtended
    }
    courseExtended
  }

  override def update(courseExtended: CourseExtended): CourseExtended = {
    execSync {
      coursesExtended.filter(_.courseId === courseExtended.courseId).map(
        s => (
          s.longDescription, s.userLimit, s.beginDate, s.endDate)
      ).update(
        courseExtended.longDescription, courseExtended.userLimit, courseExtended.beginDate, courseExtended.endDate
      )
    }
    courseExtended
  }

  override def isExist(id: Long): Boolean = execSync {
    coursesExtended.filter(_.courseId === id).exists.result
  }
}