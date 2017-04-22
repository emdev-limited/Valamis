package com.arcusys.valamis.updaters.version320

import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend
import com.arcusys.valamis.updaters.version320.schema3205.CourseExtendedTableComponent
import org.joda.time.DateTime

class DBUpdater3205Test
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


  before {
    createDB()
    execSync(coursesExtended.schema.create)
  }
  after {
    dropDB()
  }

  test("should add row with optional fields") {
    execSync(coursesExtended += (123, Some("long description"), Some(999), Some(DateTime.now()), Some(DateTime.now())))
  }

  test("should add row with only required fields") {
    execSync(coursesExtended += (123, None, None, None, None))
  }
}