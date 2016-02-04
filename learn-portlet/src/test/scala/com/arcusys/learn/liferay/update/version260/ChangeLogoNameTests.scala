package com.arcusys.learn.liferay.update.version260

import java.sql.Connection
import javax.sql.DataSource
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.valamis.storyTree.StoryTreeTableComponent
import com.arcusys.valamis.storyTree.model.Story
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.{JdbcProfile, JdbcDriver, H2Driver}
import scala.slick.jdbc.JdbcBackend

class ChangeLogoNameTests
  extends FunSuite
  with BeforeAndAfter{

  val db = Database.forURL("jdbc:h2:mem:changeLogoNameTests", driver = "org.h2.Driver")
  var connection: Connection = _

  val update = new DBUpdater2504(new SlickDBInfo{
    def dataSource: DataSource = ???
    def databaseDef: JdbcBackend#DatabaseDef = db
    def slickProfile: JdbcProfile = H2Driver
    def slickDriver: JdbcDriver = H2Driver
  })

  // db data will be released after connection close
  before {
    connection = db.createConnection()
    tables.createSchema()
  }
  after {
    connection.close()
  }

  val tables = new StoryTreeTableComponent {
    override protected val driver = H2Driver
    
    def createSchema() {
      db.withSession { implicit s => trees.ddl.create }
    }
  }

  test("change name") {
    db.withSession { implicit s =>
      tables.trees ++= Seq(
        new Story(None, 12, "t1", "d", Some("logo1.jpg"), false),
        new Story(None, 12, "t2", "d", None, false),
        new Story(None, 12, "t3", "d", Some("files/ttt/l.jpg"), false)
      )
    }

    update.doUpgrade()

    db.withSession { implicit s =>
      val t1 = tables.trees.filter(_.title === "t1").first
      assert(t1.logo.get == "logo1.jpg")

      val t2 = tables.trees.filter(_.title === "t2").first
      assert(t2.logo.isEmpty)

      val t3 = tables.trees.filter(_.title === "t3").first
      assert(t3.logo.get == "l.jpg")
    }
  }

  test("change name many times") {
    db.withSession { implicit s =>
      tables.trees ++= Seq(
        new Story(None, 12, "t1", "d", Some("logo1.jpg"), false),
        new Story(None, 12, "t2", "d", None, false),
        new Story(None, 12, "t3", "d", Some("files/ttt/l.jpg"), false)
      )
    }

    for(i <- 1 to 10) update.doUpgrade()

    db.withSession { implicit s =>
      val t1 = tables.trees.filter(_.title === "t1").first
      assert(t1.logo.get == "logo1.jpg")

      val t2 = tables.trees.filter(_.title === "t2").first
      assert(t2.logo.isEmpty)

      val t3 = tables.trees.filter(_.title === "t3").first
      assert(t3.logo.get == "l.jpg")
    }
  }
}
