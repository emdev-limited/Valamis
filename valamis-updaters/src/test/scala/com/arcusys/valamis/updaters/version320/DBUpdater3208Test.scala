package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.version320.schema3208.CourseInstructorTableComponent
import java.sql.{Connection, SQLException}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

class DBUpdater3208Test
  extends FunSuite
    with CourseInstructorTableComponent
    with BeforeAndAfter
    with SlickProfile
    with SlickDbTestBase
    with DatabaseLayer {

  import driver.api._

  val updater = new DBUpdater3208 {
    override lazy val dbInfo: SlickDBInfo = new SlickDBInfo {
      override def slickDriver: JdbcDriver = DBUpdater3208Test.this.driver

      override def slickProfile: JdbcProfile = slickDriver

      override def databaseDef: JdbcBackend#DatabaseDef = DBUpdater3208Test.this.db
    }
    override lazy val driver = DBUpdater3208Test.this.driver
  }

  before {
    createDB()
    updater.doUpgrade()
  }
  after {
    dropDB()
  }

  test("add row to table") {
    execSync(courseInstructors += CourseInstructor(123L, 456L, DateTime.now))
    assert(execSync(courseInstructors.length.result) == 1)
  }

  test("add rows with same courseId and instructorId") {
    val row = CourseInstructor(123L, 456L, DateTime.now)
    execSync(courseInstructors += row)

    intercept[SQLException] {
      execSync(courseInstructors += row)
    }
    assert(execSync(courseInstructors.length.result) == 1)
  }
}