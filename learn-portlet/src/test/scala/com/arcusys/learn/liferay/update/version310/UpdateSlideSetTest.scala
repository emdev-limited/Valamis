package com.arcusys.learn.liferay.update.version310

import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.escalatesoft.subcut.inject.NewBindingModule
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UpdateSlideSetTest extends FunSuite with BeforeAndAfter with SlickDbTestBase {
  import driver.api._

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db
      def slickDriver: JdbcDriver = driver
      def slickProfile: JdbcProfile = driver
    }
  })

  before {
    createDB()
    Await.result(OldTables.createSchema, Duration.Inf)
  }
  after {
    dropDB()
  }


  val OldTables = new slides.SlideSetTableComponent with SlickProfile {
    val driver: JdbcProfile = UpdateSlideSetTest.this.driver

    import driver.api._

    def createSchema = db.run {
      (slideSets.schema).create
    }
  }

  val updater = new DBUpdater3101(bindingModule)

  test("update slide sets") {
    val slideSet = OldTables.SlideSet(1L, "title", "description", 1L, None, false, false, None, None, None, "player title", false, "http://activityId", "draft", 1.0, new DateTime(), false)
    val slideSetInsert = OldTables.slideSets += slideSet
    
    Await.result(db.run {
      slideSetInsert
    }, Duration.Inf)

    updater.doUpgrade()

    val slideSetsTable = new slides3101.SlideSetTableComponent
      with SlickProfile {
      val driver: JdbcProfile = UpdateSlideSetTest.this.driver

    }

    val data = Await.result(db.run {
      slideSetsTable.slideSets.result
    }, Duration.Inf)

    assert(data.head.lockDate.isEmpty)
    assert(data.head.lockUserId.isEmpty)
    assert(!data.head.requiredReview)
  }
}

