package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.lesson.{LessonGradeTableComponent, LessonTableComponent}
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.escalatesoft.subcut.inject.NewBindingModule
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.H2Driver
import slick.driver.JdbcDriver
import slick.driver.JdbcProfile
import slick.jdbc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UpdateLessonGradeTest extends FunSuite with BeforeAndAfter{

  val driver = H2Driver

  import driver.api._

  val db = Database.forURL("jdbc:h2:mem:lessonGrades", driver = "org.h2.Driver")
  var connection: Connection = _

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db
      def slickDriver: JdbcDriver = driver
      def slickProfile: JdbcProfile = driver
    }
  })

  before {
    connection = db.source.createConnection()
    Await.result(tables.createSchema, Duration.Inf)
  }
  after {
    connection.close()
  }

  val tables = new LessonGradeTableComponent with SlickProfile with LessonTableComponent{
    val driver: JdbcProfile = H2Driver
    import driver.api._
    def createSchema = db.run {
      (lessons.schema ++ lessonGrades.schema).create
    }
  }

  val updater = new DBUpdater3011(bindingModule)

  test("update lesson grades") {
    val lesson = tables.Lesson(1L,LessonType.Tincan,"title","description", None,123L,None, None, None, 2L, new DateTime, false, 0.7)

    val lessonInsert = tables.lessons += lesson

    val lessonGrades = tables.LessonGrade(1L,20197L,Some(40),new DateTime, None) ::
      tables.LessonGrade(1L,20198L,None,new DateTime, None) ::
      tables.LessonGrade(1L,20199L,Some(0),new DateTime, None) :: Nil

    val lessonGradeInsert = tables.lessonGrades ++= lessonGrades

    Await.result(db.run {lessonInsert >> lessonGradeInsert}, Duration.Inf)

    updater.doUpgrade()

    val data = Await.result(db.run {tables.lessonGrades.result}, Duration.Inf)

    assert((data.filter(_.userId === 20197L).head.grade.get - 0.4).abs < 0.1)
    assert(data.filter(_.userId == 20198L).head.grade.isEmpty)
    assert((data.filter(_.userId == 20199L).head.grade.get - 0).abs < 0.1)

  }
}

