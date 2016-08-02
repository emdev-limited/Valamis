package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.certificate.{CertificateTableComponent => OldTableComponent}
import com.arcusys.learn.liferay.update.version300.certificate3004.{CertificateTableComponent => NewableComponent}
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.escalatesoft.subcut.inject.NewBindingModule
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend


class DeleteOptionGoalsTest extends FunSuite with BeforeAndAfter {

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

  val oldTable = new OldTableComponent with SlickProfile{
    val driver: JdbcProfile = H2Driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificates.ddl.create }
  }

  val newTable = new NewableComponent with SlickProfile {
    val driver: JdbcProfile = H2Driver
  }

  val updater = new DBUpdater3007(bindingModule)

  test("delete column optionalGoals") {
    updater.doUpgrade()

    val id  = db.withTransaction{ implicit s =>
      val entity = newTable.Certificate(
        id = 1L,
        title = "title",
        description = "description",
        companyId = 123L,
        createdAt = new DateTime())
      newTable.certificates returning newTable.certificates.map(_.id) insert entity
    }

    val stored = db.withSession{ implicit s =>
      newTable.certificates
        .filter(_.id === id)
        .firstOption
        .getOrElse(fail("no certificates was created"))
    }

    assert(stored.title == "title")
    assert(stored.companyId == 123L)
  }

}
