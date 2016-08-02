package com.arcusys.learn.liferay.update.version270

import java.sql.Connection

import com.arcusys.learn.liferay.update.version270.migrations.ActivityToStatementMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.settings.ActivityToStatementTableComponent

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.driver.H2Driver.simple._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.jdbc.StaticQuery
import scala.util.Try

class ActivityToStatementMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with SlickProfile
    with ActivityToStatementTableComponent {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:activitytostatement", driver = "org.h2.Driver")
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
        create table Learn_LFSiteDependentConfig(
        id	bigint(20) not null primary key,
        siteID	int(11) not null,
        dataKey	varchar(512) null,
        dataValue	varchar(512) null)"""
    ).execute
  }

  private def insertData(id: Int,
                         siteId: Int,
                         key: Option[String],
                         value: Option[String])
                        (implicit s: scala.slick.jdbc.JdbcBackend#SessionDef): Unit = {
    val keyVal = key.map("'" + _ + "'").getOrElse("null")
    val valueVal = value.map("'" + _ + "'").getOrElse("null")
    StaticQuery.updateNA(
      s"""
        insert into Learn_LFSiteDependentConfig
        ( id, siteID, dataKey, dataValue)
        values ( $id, $siteId, $keyVal, $valueVal)"""
    ).execute
  }

  test("migrate ActivityToStatement with correct data") {
    db.withTransaction { implicit s =>
      createOldTable
      insertData(1, 20594, Some("com.liferay.portlet.documentlibrary.model.DLFileEntry"), Some("completed"))
    }

    val migrator = new ActivityToStatementMigration(db, driver) {
      override def getClassNameId(className: String) = 0
    }

    migrator.migrate()

    val data = db.withSession { implicit s => activityToStatement.list }

    assert(data.head.courseId == 20594)
    assert(data.head.activityClassId == 0)
    assert(data.head.verb == "completed")
  }

  test("migrate ActivityToStatement with empty data") {
    db.withSession { implicit s =>
      createOldTable
      insertData(2, 20594, None, None)
    }

    val migrator = new ActivityToStatementMigration(db, driver) {
      override def getClassNameId(className: String) = 0
    }

    Try(migrator.migrate())

    db.withSession { implicit s =>
      assert(activityToStatement.length.run == 0)
    }
  }

}
