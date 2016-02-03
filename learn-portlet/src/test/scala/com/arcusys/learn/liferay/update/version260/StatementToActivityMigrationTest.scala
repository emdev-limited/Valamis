package com.arcusys.learn.liferay.update.version260

import java.sql.Connection
import com.arcusys.learn.liferay.update.version260.migrations.StatementToActivityMigration
import com.arcusys.valamis.settings.StatementToActivityTableComponent
import org.scalatest.{BeforeAndAfter, FunSuite}
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend


class StatementToActivityMigrationTest extends FunSuite with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:statementtoactivity", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.createConnection()
    tables.createSchema()
  }
  after {
    connection.close()
  }

  val tables = new StatementToActivityTableComponent {
    override protected val driver = H2Driver

    def createSchema() {
      db.withSession { implicit s => statementToActivity.ddl.create }
    }
  }

  test("create statementToActivity") {

    val migrator = new StatementToActivityMigration(db, H2Driver) {
      override def getOldData(implicit session: JdbcBackend#Session) = List(
        new OldEntity(1L, Some(33L), Some("Migration"), Some("mappedActivity"), Some("mappedVerb"))
      )
    }

    db.withSession { implicit s =>
      migrator.migrate()
    }
    
    val stored = db.withSession { implicit s=>
      tables.statementToActivity.filter(_.courseId === 33L).firstOption
        .getOrElse(fail("no statementToActivity created"))
    }

    assert(stored.title == "Migration")
    assert(stored.mappedActivity.contains("mappedActivity"))
    assert(stored.mappedVerb.contains("mappedVerb"))
  }

  test("create empty statementToActivity") {

    val migrator = new StatementToActivityMigration(db, H2Driver) {
      override def getOldData(implicit session: JdbcBackend#Session) = List(
        new OldEntity(2L, None, None, None, None)
      )
    }

    db.withSession { implicit s =>
      migrator.migrate()
    }

    val stored = db.withSession { implicit s=>
      tables.statementToActivity.filter(_.courseId === -1L).firstOption
        .getOrElse(fail("no statementToActivity created"))
    }

    assert(stored.courseId == -1L)
    assert(stored.title == "")
    assert(stored.mappedActivity isEmpty)
    assert(stored.mappedVerb isEmpty)
  }
}
