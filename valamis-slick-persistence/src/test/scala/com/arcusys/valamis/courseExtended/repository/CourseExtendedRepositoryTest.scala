package com.arcusys.valamis.courseExtended.repository

import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.valamis.course.model.CourseExtended
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.persistence.impl.course.CourseExtendedRepositoryImpl
import com.arcusys.valamis.persistence.impl.course.schema.CourseExtendedTableComponent
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend
import org.joda.time.DateTime


class CourseExtendedRepositoryTest
  extends FunSuite
    with CourseExtendedTableComponent
    with BeforeAndAfter
    with SlickProfile
    with SlickDbTestBase
    with DatabaseLayer {

  import driver.api._

  lazy val dbInfo: SlickDBInfo = new SlickDBInfo {
    override def slickDriver: JdbcDriver = driver

    override def slickProfile: JdbcProfile = slickDriver

    override def databaseDef: JdbcBackend#DatabaseDef = db
  }

  implicit val dialect = Dialect(dbInfo.slickDriver)
    .getOrElse(throw new Exception(s"There is no dialect for driver ${dbInfo.slickDriver}"))

  val coursesExtendedRepository = new CourseExtendedRepositoryImpl(db, driver)

  val sampleDate = DateTime.now //.parse("2016-12-21T10:45:05.000+03:00")

  var course1Extended = CourseExtended(
    courseId = 123,
    longDescription = Some("description1"),
    userLimit = Some(123),
    beginDate = Some(sampleDate),
    endDate = Some(sampleDate.plusMonths(1))
  )

  before {
    createDB()
    execSync(coursesExtended.schema.create)
    coursesExtendedRepository.create(course1Extended)
  }
  after {
    dropDB()
  }

  test("should get course by id") {
    val course = coursesExtendedRepository.getById(course1Extended.courseId)
    assert(course.isDefined)
  }

  test("should get updated course by id") {
    val newLongDescription = Some("new long description")
    coursesExtendedRepository.update(course1Extended.copy(longDescription = newLongDescription))
    val course = coursesExtendedRepository.getById(course1Extended.courseId)
    assert(course.get.longDescription == newLongDescription)
  }

  test("should check if course exists by id") {
    val isExist = coursesExtendedRepository.isExist(course1Extended.courseId)
    assert(isExist)
  }

  test("should check if course not exists by id") {
    val isExist = coursesExtendedRepository.isExist(course1Extended.courseId + 1)
    assert(!isExist)
  }

  test("delete course extended row") {
    coursesExtendedRepository.delete(course1Extended.courseId)
    val course = coursesExtendedRepository.getById(course1Extended.courseId)
    assert(course.isEmpty)
  }
}