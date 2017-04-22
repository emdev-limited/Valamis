package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.update.version240.storyTree.{StoryTreeTableComponent => OldTableComponent}
import com.arcusys.learn.liferay.update.version240.storyTree.{StoryTreeTableComponent => NewTableComponent}
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.escalatesoft.subcut.inject.NewBindingModule
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class DBUpdater2516Tests
  extends FunSuite
    with BeforeAndAfter
    with SlickDbTestBase {

  val slickTestDriver = driver
  import slickTestDriver.simple._

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db
      def slickDriver: JdbcDriver = driver
      def slickProfile: JdbcProfile = driver
    }
  })

  before {
    createDB()
  }
  after {
    dropDB()
  }

  val oldTables = new OldTableComponent {
    val driver: JdbcProfile = slickTestDriver
    import driver.simple._
    def createSchema(): Unit = db.withSession { implicit s => trees.ddl.create }
  }

  val newTables = new NewTableComponent with SlickProfile {
    val driver: JdbcProfile = slickTestDriver
  }

  val updater = new DBUpdater2516(bindingModule)

  test("threshold test") {
    assert(2516 == updater.getThreshold)
  }

  test("add column test") {
    oldTables.createSchema()

    updater.doUpgrade()

    db.withSession{ implicit s =>
      newTables.trees.list
    }
  }

  test("add column test 2") {
    oldTables.createSchema()

    db.withSession{ implicit s =>
      oldTables.trees += (None, 10, "title", "description", Some("logo"), false)
    }

    updater.doUpgrade()

    val data = db.withSession{ implicit s =>
      newTables.trees.list
    }

    assert(data.size == 1)
    assert(!data.head._6)
  }
}
