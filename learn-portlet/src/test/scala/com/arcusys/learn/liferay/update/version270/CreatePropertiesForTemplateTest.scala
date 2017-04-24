package com.arcusys.learn.liferay.update.version270

import java.sql.Connection

import com.arcusys.learn.liferay.update.version270.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.escalatesoft.subcut.inject.NewBindingModule
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class CreatePropertiesForTemplateTest extends FunSuite with BeforeAndAfter with SlickDbTestBase {

  import driver.simple._

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db
      def slickDriver: JdbcDriver = driver
      def slickProfile: JdbcProfile = driver
    }
  })

  before {
    createDB()
    table.createSchema()
  }
  after {
    dropDB()
  }

  val table = new SlideTableComponent {
    override protected val driver: JdbcProfile = CreatePropertiesForTemplateTest.this.driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      (slides.ddl ++ slideSets.ddl ++ slideElements.ddl ++ slideElementProperties.ddl).create
    }
  }

  import table._
  val updater = new DBUpdater2711(bindingModule)
  val slideSet = SlideSet(title = "title", description = "description", courseId = 1L)

  test("create properties for template <Text and image>") {
    import table._
    val slide = Slide(title = "Text and image", slideSetId = -1, isTemplate = true)
    val slideElement = SlideElement(None, "1", "old content", "text", -1)

    val elementId = db.withSession { implicit s =>
      val slideSetId = (slideSets returning slideSets.map(_.id)) += slideSet
      val slideId = (slides returning slides.map(_.id)) += slide.copy(slideSetId = slideSetId)
      (slideElements returning slideElements.map(_.id)) += slideElement.copy(slideId = slideId)
    }

    updater.doUpgrade()

    val stored = db.withSession { implicit s =>
      slideElementProperties.filter(_.slideElementId === elementId).list
    }

    assert(stored.size == 4)
  }

  test("create properties for non existing template"){
    import table._
    val slide = Slide(title = "Wrong name", slideSetId = -1, isTemplate = true)
    val slideElement = SlideElement(None, "1", "old content", "text", -1)

    val elementId = db.withSession { implicit s =>
      val slideSetId = (slideSets returning slideSets.map(_.id)) += slideSet
      val slideId = (slides returning slides.map(_.id)) += slide.copy(slideSetId = slideSetId)
      (slideElements returning slideElements.map(_.id)) += slideElement.copy(slideId = slideId)
    }

    updater.doUpgrade()

    val stored = db.withSession { implicit s =>
      slideElementProperties.filter(_.slideElementId === elementId).list
    }

    assert(stored.isEmpty)
  }
}
