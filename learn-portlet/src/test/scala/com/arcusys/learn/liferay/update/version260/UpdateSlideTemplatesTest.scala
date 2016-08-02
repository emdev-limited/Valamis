package com.arcusys.learn.liferay.update.version260

import java.sql.Connection

import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo

import scala.slick.driver.{H2Driver, JdbcDriver, JdbcProfile}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.jdbc.JdbcBackend

class UpdateSlideTemplatesTest extends FunSuite with BeforeAndAfter {

  val driver = H2Driver
  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:templates", driver = "org.h2.Driver")
  var connection: Connection = _

  val dbInfo = new SlickDBInfo {
    def databaseDef: JdbcBackend#DatabaseDef = db    
    def slickProfile: JdbcProfile = driver
    def slickDriver: JdbcDriver = driver
  }
  
  before {
    connection = db.source.createConnection()
    table.createSchema()
  }
  after {
    connection.close()
  }

  val table = new SlideTableComponent {
    override protected val driver: JdbcProfile = H2Driver
    
    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      (slides.ddl ++ slideThemes.ddl ++ slideSets.ddl ++ slideElements.ddl).create 
    }
  }

  import table._
  val updater = new DBUpdater2517(dbInfo)
  val slideSet = SlideSet(title = "title", description = "description", courseId = 1L)

  test("update template <Text and image>") {
    import table._
    val slide = Slide(title = "Text and image", slideSetId = -1, isTemplate = true)
    val slideElement = SlideElement(None, "1", "1", "1", "1", "1", "old content", "text", -1)

    val elementId = db.withSession { implicit s =>
      val slideSetId = (slideSets returning slideSets.map(_.id)) += slideSet
      val slideId = (slides returning slides.map(_.id)) += slide.copy(slideSetId = slideSetId)
      (slideElements returning slideElements.map(_.id)) += slideElement.copy(slideId = slideId)
    }

    updater.doUpgrade()

    val stored = db.withSession { implicit s =>
      slideElements.filter(_.id === elementId).firstOption
        .getOrElse(fail("no element created"))
    }

    assert(stored.content == "<div><strong><span style=\"font-size:3em\">Page header</span></strong></div>")
  }

  test("update template <Title and subtitle>") {
    import table._
    val slide = Slide(title = "Title and subtitle", slideSetId = -1, isTemplate = true)
    val slideElement = SlideElement(None, "1", "1", "1", "1", "2", "old content", "text", -1)

    val elementId = db.withSession { implicit s =>
      val slideSetId = (slideSets returning slideSets.map(_.id)) += slideSet
      val slideId = (slides returning slides.map(_.id)) += slide.copy(slideSetId = slideSetId)
      (slideElements returning slideElements.map(_.id)) += slideElement.copy(slideId = slideId)
    }

    updater.doUpgrade()

    val stored = db.withSession { implicit s =>
      slideElements.filter(_.id === elementId).firstOption
        .getOrElse(fail("no element created"))
    }

    assert(stored.content == "<div><span style=\"font-size:1.25em\">Page subtitle</span></div>")
  }

  test("update not existing template") {
    import table._
    val slide = Slide(title = "Text and image", slideSetId = -1, isTemplate = false)
    val slideElement = SlideElement(None, "1", "1", "1", "1", "1", "old content", "text", -1)

    val elementId = db.withSession { implicit s =>
      val slideSetId = (slideSets returning slideSets.map(_.id)) += slideSet
      val slideId = (slides returning slides.map(_.id)) += slide.copy(slideSetId = slideSetId)
      (slideElements returning slideElements.map(_.id)) += slideElement.copy(slideId = slideId)
    }

    updater.doUpgrade()

    val stored = db.withSession { implicit s =>
      slideElements.filter(_.id === elementId).firstOption
        .getOrElse(fail("no element created"))
    }

    assert(stored.content == "old content")
  }
}
