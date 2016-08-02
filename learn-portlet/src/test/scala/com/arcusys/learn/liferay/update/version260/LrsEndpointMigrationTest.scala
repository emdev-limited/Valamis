package com.arcusys.learn.liferay.update.version260

import java.sql.Connection

import com.arcusys.learn.liferay.update.version260.migrations.LrsEndpointMigration
import com.arcusys.valamis.persistence.impl.lrs.LrsEndpointTableComponent
import com.arcusys.valamis.lrsEndpoint.model.AuthType
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.jdbc.JdbcBackend
import scala.util.Try

class LrsEndpointMigrationTest extends FunSuite with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:lrsendpoint", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    table.createSchema()
  }
  after {
    connection.close()
  }

  val table = new LrsEndpointTableComponent with SlickProfile {
    override val driver = H2Driver

    def createSchema() {
      db.withSession { implicit s => lrsEndpoint.ddl.create }
    }
  }
  test("create lrsEndpoint") {
    val migrator = new LrsEndpointMigration(db, H2Driver) {
      override def getOldData(implicit session: JdbcBackend#Session) = List(
        new OldEntity(1L, Some("/valamis-lrs-portlet/test"), Some("Basic"), Some("11111"), Some("22222"),Some(""))
      )
    }

    db.withSession { implicit s =>
      migrator.migrate()
    }

    val stored = db.withSession { implicit s=>
      table.lrsEndpoint.filter(_.secret === "22222").firstOption
        .getOrElse(fail("no lrsEndpoint created"))
    }

    assert(stored.endpoint == "/valamis-lrs-portlet/test")
    assert(stored.auth == AuthType.BASIC)
    assert(stored.customHost isEmpty)
    assert(stored.key.contains("11111"))

  }

  test("create lrsEndpoint with empty endpoint and auth"){
    val migrator = new LrsEndpointMigration(db, H2Driver) {
      override def getOldData(implicit session: JdbcBackend#Session) = List(
        new OldEntity(2L, None, None, None, None,None)
      )
    }

    db.withSession { implicit s=>
      migrator.migrate()
    }

    db.withSession { implicit s=>
      assert(table.lrsEndpoint.length.run == 0)
    }
  }

  test("create lrsEndpoint with incorrect authType"){
    val migrator = new LrsEndpointMigration(db, H2Driver) {
      override def getOldData(implicit session: JdbcBackend#Session) = List(
        new OldEntity(3L, Some("/valamis-lrs-portlet/test"), Some("Basic_User"), Some("test"), Some("test"), Some("test"))
      )
    }

    Try(migrator.migrate())


    db.withSession { implicit s=>
      assert(table.lrsEndpoint.length.run == 0)
    }
  }
}
