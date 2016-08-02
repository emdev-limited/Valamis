package com.arcusys.learn.liferay.update.version270

import java.sql.Connection

import com.arcusys.learn.liferay.update.version260.slide.{SlideTableComponent => OldTableComponent}
import com.arcusys.learn.liferay.update.version270.slide.{SlideTableComponent => NewTableComponent}
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.escalatesoft.subcut.inject.NewBindingModule
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class DeleteElementAttributesTest extends FunSuite with BeforeAndAfter{

  val driver = H2Driver
  import driver.simple._
  val db = Database.forURL("jdbc:h2:mem:deleteelementattributes", driver = "org.h2.Driver")
  var connection: Connection = _

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db
      def slickDriver: JdbcDriver = driver
      def slickProfile: JdbcProfile = driver
    }
  })

  before {
    connection = db.source.createConnection()
    oldTable.createSchema()
  }
  after {
    connection.close()
  }

  val oldTable = new OldTableComponent {
    protected val driver: JdbcProfile = H2Driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      slideElements.ddl.create }
  }

  val newTable = new NewTableComponent {
    protected val driver: JdbcProfile = H2Driver
  }

  val updater = new DBUpdater2710(bindingModule)

  test("delete columns") {
    updater.doUpgrade()

    val element = newTable.SlideElement(None, "1", "content", "text", 10L)

    val id  = db.withSession{ implicit s =>
      newTable.slideElements returning newTable.slideElements.map(_.id) += element
    }

    val stored = db.withSession{ implicit s =>
      newTable.slideElements
        .filter(_.id === id)
        .firstOption
        .getOrElse(fail("no slideEement was created"))
    }

    assert(stored.slideEntityType == "text")
    assert(stored.content == "content")
  }
}
