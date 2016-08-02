package com.arcusys.learn.liferay.update.version270

import java.sql.Connection

import com.arcusys.learn.liferay.update.version270.migrations.TincanUriMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.uri.TincanUriTableComponent
import com.arcusys.valamis.uri.model.{TincanURI, TincanURIType}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.StaticQuery
import scala.util.Try

class TincanUriMigrationTest(val driver: JdbcProfile)
  extends FunSuite
  with BeforeAndAfter
  with SlickProfile
  with TincanUriTableComponent {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:tincanuri", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
  }
  after {
    connection.close()
  }

  private def createOldTable(implicit s: scala.slick.jdbc.JdbcBackend#SessionDef): Unit = {
    StaticQuery.updateNA(
      """
        create table Learn_LFTincanURI(
        uri VARCHAR(200) not null primary key,
        objID VARCHAR(512) null,
        objType VARCHAR(512) null,
        content VARCHAR(2000) null)"""
    ).execute
  }

  private def insertData(uri: String,
                         objId: String,
                         objType: String,
                         content: String)
                        (implicit s: scala.slick.jdbc.JdbcBackend#SessionDef): Unit = {
    StaticQuery.updateNA(
      s"""
        insert into Learn_LFTincanURI
        ( uri, objID, objType, content)
        values ( '$uri', '$objId', '$objType', '$content')"""
    ).execute
  }

  test("migrate TincanUri with correct data") {
    db.withSession { implicit s =>
      createOldTable
      insertData("a", "b", "verb", "c")
    }

    new TincanUriMigration(db, driver).migrate()

    val data = db.withSession { implicit s => tincanUris.list }

    assert(data.size == 1)

    val entity = data.head
    assert(entity.uri == "a")
    assert(entity.objId == "b")
    assert(entity.objType == TincanURIType.Verb)
    assert(entity.content == "c")
  }

  test("migrate list of TincanUri") {
    db.withSession { implicit s =>
      createOldTable
      insertData("uri1", "1", "activity", "content")
      insertData("uri2", "2", "package", "content")
      insertData("uri3", "3", "course", "content")
      insertData("uri4", "4", "category", "content")
    }
    new TincanUriMigration(db, driver).migrate()

    db.withSession { implicit s =>
      val data = tincanUris.list

      assert(data.size == 4)

      val r1 = data.find(_.objId == "1") getOrElse fail("no TincanUri created")
      assert(r1 == TincanURI("uri1", "1", TincanURIType.Activity, "content"))

      val r2 = data.find(_.objId == "2") getOrElse fail("no TincanUri created")
      assert(r2 == TincanURI("uri2", "2", TincanURIType.Package, "content"))

      val r3 = data.find(_.objId == "3") getOrElse fail("no TincanUri created")
      assert(r3 == TincanURI("uri3", "3", TincanURIType.Course, "content"))

      val r4 = data.find(_.objId == "4") getOrElse fail("no TincanUri created")
      assert(r4 == TincanURI("uri4", "4", TincanURIType.Category, "content"))
    }

  }

  test("migrate TincanUri with incorrect type") {
    db.withSession { implicit s =>
      createOldTable
      insertData("uri", "1", "wrong type", "content")
    }

    Try(new TincanUriMigration(db, driver).migrate())

    db.withSession { implicit s =>
      assert(tincanUris.length.run == 0)
    }
  }
}
