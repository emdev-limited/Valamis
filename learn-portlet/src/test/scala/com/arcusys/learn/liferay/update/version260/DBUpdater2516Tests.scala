package com.arcusys.learn.liferay.update.version260

import java.sql.Connection


import com.arcusys.learn.liferay.update.version240.storyTree.{StoryTreeTableComponent => OldTableComponent}
import com.arcusys.valamis.storyTree.{StoryTreeTableComponent => NewTableComponent}

import com.arcusys.valamis.core.SlickDBInfo
import com.escalatesoft.subcut.inject.NewBindingModule
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{JdbcDriver, JdbcProfile, H2Driver}
import scala.slick.jdbc.JdbcBackend

class DBUpdater2516Tests extends FunSuite with BeforeAndAfter {

  val slickTestDriver = H2Driver
  import slickTestDriver.simple._
  val db = Database.forURL("jdbc:h2:mem:DBUpdater2516Tests", driver = "org.h2.Driver")
  var connection: Connection = _

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db
      def slickDriver: JdbcDriver = slickTestDriver
      def slickProfile: JdbcProfile = slickTestDriver
    }
  })

  before {
    connection = db.createConnection()
  }
  after {
    connection.close()
  }

  val oldTables = new OldTableComponent {
    protected val driver: JdbcProfile = slickTestDriver
    import driver.simple._
    def createSchema(): Unit = db.withSession { implicit s => trees.ddl.create }
  }

  val newTables = new NewTableComponent {
    protected val driver: JdbcProfile = slickTestDriver
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
    assert(!data.head.isDefault)
  }
}
