package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.update.version260.migrations.LrsEndpointMigration
import com.arcusys.valamis.persistence.impl.lrs.LrsEndpointTableComponent
import com.arcusys.valamis.lrsEndpoint.model.AuthType
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.jdbc.JdbcBackend
import scala.util.Try

class LrsEndpointMigrationTest
  extends FunSuite
    with BeforeAndAfter
    with SlickDbTestBase {

  import driver.simple._

  before {
    createDB()
    table.createSchema()
  }
  after {
    dropDB()
  }

  val table = new LrsEndpointTableComponent with SlickProfile {
    override val driver = LrsEndpointMigrationTest.this.driver

    def createSchema() {
      import driver.simple._
      db.withSession { implicit s => lrsEndpoint.ddl.create }
    }
  }
  test("create lrsEndpoint") {
    val migrator = new LrsEndpointMigration(db, driver) {
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
    val migrator = new LrsEndpointMigration(db, driver) {
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
    val migrator = new LrsEndpointMigration(db, driver) {
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
