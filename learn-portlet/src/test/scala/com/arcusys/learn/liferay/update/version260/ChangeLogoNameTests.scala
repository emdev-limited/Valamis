package com.arcusys.learn.liferay.update.version260

import java.sql.Connection
import javax.sql.DataSource

import com.arcusys.learn.liferay.update.version260.storyTree.StoryTreeTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class ChangeLogoNameTests
  extends FunSuite
    with StoryTreeTableComponent
    with SlickProfile
    with BeforeAndAfter
    with SlickDbTestBase {

  import driver.simple._
  val update = new DBUpdater2504(new SlickDBInfo {
    def databaseDef: JdbcBackend#DatabaseDef = db
    def slickProfile: JdbcProfile = driver
    def slickDriver: JdbcDriver = driver
  })

  // db data will be released after connection close
  before {
    createDB()
    createSchema()
  }
  after {
    dropDB()
  }


  def createSchema() {
    db.withSession { implicit s => trees.ddl.create }
  }

  test("change name") {
    db.withSession { implicit s =>
      trees ++= Seq(
        new Story(None, 12, "t1", "d", Some("logo1.jpg"), false),
        new Story(None, 12, "t2", "d", None, false),
        new Story(None, 12, "t3", "d", Some("files/ttt/l.jpg"), false)
      )
    }

    update.doUpgrade()

    db.withSession { implicit s =>
      val t1 = trees.filter(_.title === "t1").first
      assert(t1.logo.get == "logo1.jpg")

      val t2 = trees.filter(_.title === "t2").first
      assert(t2.logo.isEmpty)

      val t3 = trees.filter(_.title === "t3").first
      assert(t3.logo.get == "l.jpg")
    }
  }

  test("change name many times") {
    db.withSession { implicit s =>
      trees ++= Seq(
        new Story(None, 12, "t1", "d", Some("logo1.jpg"), false),
        new Story(None, 12, "t2", "d", None, false),
        new Story(None, 12, "t3", "d", Some("files/ttt/l.jpg"), false)
      )
    }

    for (i <- 1 to 10) update.doUpgrade()

    db.withSession { implicit s =>
      val t1 = trees.filter(_.title === "t1").first
      assert(t1.logo.get == "logo1.jpg")

      val t2 = trees.filter(_.title === "t2").first
      assert(t2.logo.isEmpty)

      val t3 = trees.filter(_.title === "t3").first
      assert(t3.logo.get == "l.jpg")
    }
  }
}
