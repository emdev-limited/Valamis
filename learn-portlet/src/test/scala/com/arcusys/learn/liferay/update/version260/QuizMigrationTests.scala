package com.arcusys.learn.liferay.update.version260

import java.sql.Connection

import com.arcusys.learn.liferay.update.version260.model.{LFQuiz, LFQuizQuestion}
import com.arcusys.learn.liferay.update.version260.slide.SlideTableComponent
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.jdbc.JdbcBackend
import scala.util.Try

class QuizMigrationTests extends FunSuite with BeforeAndAfter with SlickDbTestBase {

  val fileService = null
  val migration = new QuizMigration(db, driver, fileService)

  import driver.simple._

  // db data will be released after connection close
  before {
    createDB()
    createSchema()
  }
  after {
    dropDB()
  }

  val tables = new SlideTableComponent {
    override protected val driver = QuizMigrationTests.this.driver
  }

  def createSchema() {
    new SlideTableComponent {
      override protected val driver = QuizMigrationTests.this.driver
      import driver.simple._
      db.withSession { implicit s =>
        import driver.simple._
        (slides.ddl ++ slideSets.ddl ++ slideElements.ddl).create
      }
    }
  }

  test("create slideSet from quiz") {
    val quiz = new LFQuiz(1, Some("my title"), Some("My description"), None, None, None, Some(123L), Some(23454L))

    val slideSetId = db.withSession { implicit s =>
      migration.createSlideSet(quiz)
    }

    val stored = db.withSession { implicit s=>
      tables.slideSets.filter(_.id === slideSetId).firstOption
        .getOrElse(fail("no slideSet created"))
    }

    assert(stored.logo isEmpty)
    assert(stored.title == "my title")
    assert(stored.description == "My description")
    assert(stored.courseId == 123L)
    assert(stored.duration contains 23454L)
  }

  test("create slideSet from empty Quiz") {
    val quiz = new LFQuiz(1, None, None, None, None, None, None, None)

    val slideSetId = db.withSession { implicit s =>
      migration.createSlideSet(quiz)
    }

    val stored = db.withSession { implicit s =>
      tables.slideSets.filter(_.id === slideSetId).firstOption
        .getOrElse(fail("no slideSet created"))
    }

    assert(stored.logo isEmpty)
    assert(stored.title == "Lesson")
    assert(stored.description == "")
    assert(stored.courseId == -1L)
    assert(stored.duration isEmpty)
  }


  test("transaction test") {
    val migration = new QuizMigration(db, driver, fileService) {
      override def getQuizSet(implicit session: JdbcBackend#Session) = List(
        new LFQuiz(1, None, None, None, None, None, None, None),
        new LFQuiz(2, None, None, None, None, None, None, None),
        new LFQuiz(3, None, None, None, None, None, None, None),
        new LFQuiz(4, None, None, None, None, None, None, None)
      )

      override def collectQuizData(oldEntry: LFQuiz)(implicit session: JdbcBackend#Session) = {
        if (oldEntry.id != 4) List()
        else throw new Exception("test exception")
      }
    }

    Try(migration.begin())

    db.withSession { implicit s=>
      assert(tables.slideSets.length.run == 0)
    }
  }

  test("create slide with question element from quiz") {
    val question = List(
     new LFQuizQuestion(1, Some(1L), None, Some(101L), Some("QuestionBank"), Some("title"), Some(""), Some(""), Some(1L), None, None))

    val quiz = new LFQuiz(1, Some("my title"), Some("My description"), None, None, None, Some(123L), Some(23454L))

    val slide = db.withSession { implicit s =>
      val slideSetId = migration.createSlideSet(quiz)

      migration.createSlides(slideSetId, question, "normal")

      tables.slides.filter(_.slideSetId === slideSetId).firstOption
        .getOrElse(fail("no slide created"))
    }

    assert(slide.bgImage isEmpty)
    assert(slide.title == "title")
    assert(slide.duration isEmpty)
    assert(slide.leftSlideId isEmpty)
    assert(!slide.isTemplate)

    val storedElement = db.withSession{ implicit s =>
      migration.addSlideElement(question.head, slide.id.get)

      tables.slideElements.filter(_.slideId === slide.id.get).firstOption
        .getOrElse(fail("no slideElement created"))
    }

    assert(storedElement.content == "101")
    assert(storedElement.height == "auto")
    assert(storedElement.top == "0")
    assert(storedElement.slideEntityType == "question")
    assert(storedElement.notifyCorrectAnswer isEmpty)

  }

  test("create slide from empty quiz") {
    val question = List(
      new LFQuizQuestion(1, None, None, None, None, None, None, None, None, None, None))

    val quiz = new LFQuiz(1, Some("my title"), Some("My description"), None, None, None, Some(123L), Some(23454L))

    val slide = db.withSession { implicit s =>
      val slideSetId = migration.createSlideSet(quiz)

      migration.createSlides(slideSetId, question, "normal")

      tables.slides.filter(_.slideSetId === slideSetId).firstOption
        .getOrElse(fail("no slide created"))
    }

    assert(slide.bgImage isEmpty)
    assert(slide.title == "Page")
    assert(slide.duration isEmpty)
    assert(slide.leftSlideId isEmpty)
    assert(!slide.isTemplate)
  }

  test("create pptx slide from quiz") {
    val question = new LFQuizQuestion(1, None, None, None, Some("PPTX"), Some("pptx page"), None, Some("123.jpg"), Some(1L), None, None)

    val quiz = new LFQuiz(1, Some("my title"), Some("My description"), None, None, None, Some(123L), Some(23454L))

    val slide = db.withSession { implicit s =>
      val slideSetId = migration.createSlideSet(quiz)

      migration.createSlides(slideSetId, List(question), "pptx")

      tables.slides.filter(_.slideSetId === slideSetId).firstOption
        .getOrElse(fail("no slide created"))
    }

    assert(slide.bgImage.contains("123.jpg contain"))
    assert(slide.title == "pptx page")
    assert(slide.duration isEmpty)
    assert(slide.leftSlideId isEmpty)
  }
}
