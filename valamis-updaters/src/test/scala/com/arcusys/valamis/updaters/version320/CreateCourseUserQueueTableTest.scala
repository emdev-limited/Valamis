package com.arcusys.valamis.updaters.version320

import scala.concurrent.ExecutionContext.Implicits.global
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.version320.schema3206.CourseUserQueueTableComponent
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcProfile, JdbcDriver}
import slick.jdbc.JdbcBackend
import scala.util.{Success, Failure, Try}


class CreateCourseUserQueueTableTest
  extends FunSuite
    with CourseUserQueueTableComponent
    with BeforeAndAfter
    with SlickProfile
    with SlickDbTestBase {

  import driver.api._

  val updater = new DBUpdater3206 {
    override lazy val dbInfo: SlickDBInfo = new SlickDBInfo {
      override def slickDriver: JdbcDriver = CreateCourseUserQueueTableTest.this.driver

      override def slickProfile: JdbcProfile = slickDriver

      override def databaseDef: JdbcBackend#DatabaseDef = CreateCourseUserQueueTableTest.this.db
    }
    override lazy val driver = CreateCourseUserQueueTableTest.this.driver
  }

  before {
    createDB()
    updater.doUpgrade()
  }
  after {
    dropDB()
  }

  test("add row to table ") {

    val data = for {
      row <- courseUserQueueTQ +=(30304L, 25199L, new DateTime)
      res <- courseUserQueueTQ.result
    } yield res

    assert(await(db.run(data)).length === 1)

  }

  test("add rows with same userId and entityId") {
    val insertQueueRow = courseUserQueueTQ +=(30304L, 25199L, new DateTime)

    val tryInsertTwice = db.run(insertQueueRow).flatMap {
      _ => db.run(insertQueueRow)
    }

    Try {
      await(tryInsertTwice)
    } match {
      case Failure(e: Exception) =>
        val data = await(db.run(courseUserQueueTQ.result))
        assert(data.length === 1)
      case Success(_) =>
        fail("exception expected")
    }
  }

}
