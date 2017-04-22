package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version270.lesson.{LessonTableComponent => OldTable}
import com.arcusys.learn.liferay.update.version270.slide.SlideSetTableComponent
import com.arcusys.learn.liferay.update.version300.lesson.{TincanActivityTableComponent, LessonTableComponent => NewTable}
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.escalatesoft.subcut.inject.NewBindingModule
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class AddScoreAndReviewTest extends FunSuite with BeforeAndAfter with SlickDbTestBase {

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
    oldTable.createSchema()
  }
  after {
    dropDB()
  }

  val oldTable = new OldTable
    with SlideSetTableComponent
    with TincanActivityTableComponent
    with SlickProfile {
    val driver: JdbcProfile = AddScoreAndReviewTest.this.driver

    import driver.simple._

    def createSchema(): Unit = db.withSession { implicit s => (lessons.ddl ++ tincanActivitiesTQ.ddl ++ slideSets.ddl).create }
  }

  val newTable = new NewTable with SlickProfile {
    val driver: JdbcProfile = AddScoreAndReviewTest.this.driver
  }

  val updater = new DBUpdater3009(bindingModule)

  test("added score and review column in lessons table with migration") {

    val slideSets = oldTable.SlideSet(
      title = "title",
      description = "description",
      courseId = 123L,
      scoreLimit = Some(0.9),
      activityId = "http://localhost:8080/delegate/uri/course/course_2e528620-bec2-4507-ad9c-ccbf1e7828b3",
      status = "published",
      version = 1.1) :: oldTable.SlideSet(
      title = "title",
      description = "description",
      courseId = 123L,
      scoreLimit = Some(0.8),
      activityId = "http://localhost:8080/delegate/uri/course/course_2e528620-bec2-4507-ad9c-ccbf1e7828b3",
      status = "published",
      version = 1.2) :: Nil

    db.withSession { implicit s =>
      oldTable.slideSets ++= slideSets
    }

    db.withSession { implicit s =>
      oldTable.lessons insert oldTable.Lesson(1L, LessonType.Tincan, "title", "description", None, 123L, None, None, None, 3L, new DateTime())
    }

    db.withSession { implicit s =>
      oldTable.tincanActivitiesTQ insert oldTable.TincanActivity(1L,
        "http://localhost:8080/delegate/uri/course/course_2e528620-bec2-4507-ad9c-ccbf1e7828b3",
        "activityType",
        "name",
        "description",
        None,
        None)
    }

    updater.doUpgrade()

    val data = db.withSession { implicit s =>
      newTable.lessons.list
    }

    assert(data.head.scoreLimit == 0.8)
    assert(data.head.title == "title")
    assert(!data.head.requiredReview)
  }

  test("added score and review column in lessons table without migration") {

    db.withSession { implicit s =>
      oldTable.slideSets insert oldTable.SlideSet(
        title = "title",
        description = "description",
        courseId = 123L,
        scoreLimit = Some(0.9),
        activityId = "http://localhost:8080/delegate/uri/course/course_2e528620-bec2-4507-ad9c-ccbf1e7828b3",
        status = "draft",
        version = 0.1)
    }

    db.withSession { implicit s =>
      oldTable.lessons insert oldTable.Lesson(1L, LessonType.Tincan, "title", "description", None, 123L, None, None, None, 3L, new DateTime())
    }

    db.withSession { implicit s =>
      oldTable.tincanActivitiesTQ insert oldTable.TincanActivity(1L,
        "http://localhost:8080/delegate/uri/course/package_2e528620-bec2-4507-ad9c-ccbf1e7828b3",
        "activityType",
        "name",
        "description",
        None,
        None)
    }

    updater.doUpgrade()

    val data = db.withSession { implicit s =>
      newTable.lessons.list
    }

    assert(data.head.scoreLimit == 0.7)
    assert(data.head.title == "title")
    assert(!data.head.requiredReview)

  }
}