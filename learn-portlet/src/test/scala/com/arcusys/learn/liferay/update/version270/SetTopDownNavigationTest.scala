package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.update.version270.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.escalatesoft.subcut.inject.NewBindingModule
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class SetTopDownNavigationTest extends FunSuite with BeforeAndAfter with SlickDbTestBase {

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
    override protected val driver: JdbcProfile = SetTopDownNavigationTest.this.driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      (slides.ddl ++ slideSets.ddl).create
    }
  }

  val updater = new DBUpdater2704(bindingModule)

  test("set topDownNavigation to false") {
    import table._
    val slideSet = SlideSet(title = "title", description = "description", courseId = 1L)
    val slideSetId = db.withSession { implicit s =>
      slideSets returning slideSets.map(_.id) += slideSet
    }
    val slide = Slide(title = "page", slideSetId = slideSetId)
    db.withSession { implicit s =>
      slides += slide
    }
    updater.doUpgrade()

    val stored = db.withSession { implicit s =>
      slideSets.filter(_.id === slideSetId).firstOption
        .getOrElse(fail("no slideSet was created"))
    }

    assert(!stored.topDownNavigation)
  }

  test("set topDownNavigation to true") {
    import table._
    val slideSet = SlideSet(title = "title", description = "description", courseId = 1L)
    val slideSetId = db.withSession { implicit s =>
      slideSets returning slideSets.map(_.id) += slideSet
    }
    val slidesList = Slide(title = "page1", slideSetId = slideSetId) ::
      Slide(title = "page2", slideSetId = slideSetId, topSlideId = Some(2L)) ::
      Nil
    db.withTransaction { implicit s =>
      slidesList.foreach(slide =>
        slides += slide
      )
    }
    updater.doUpgrade()

    val stored = db.withSession { implicit s =>
      slideSets.filter(_.id === slideSetId).firstOption
        .getOrElse(fail("no slideSet was created"))
    }

    assert(stored.topDownNavigation)
  }

}
