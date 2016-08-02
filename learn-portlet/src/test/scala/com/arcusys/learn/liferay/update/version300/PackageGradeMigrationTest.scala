package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.PackageGradeMigration
import com.arcusys.learn.liferay.update.version270.lesson.LessonGradeTableComponent
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.learn.liferay.update.version270.lesson.LessonTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.joda.JodaDateTimeMapper
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcDriver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class PackageGradeMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with LessonGradeTableComponent
    with LessonTableComponent
    with SlickProfile {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:packageGradeTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFPackageGradeStorage (
          	userId LONG not null,
          	packageId LONG not null,
          	grade VARCHAR(512) null,
          	comment_ VARCHAR(512) null,
          	date_ DATETIME null,
          	primary key (userId, packageId)
          );"""
      ).execute

      lessons.ddl.create
    }
  }
  after {
    connection.close()
  }

  val courseId = 245
  val lessonOwnerId = 354

  test("empty source table") {
    val migration = new PackageGradeMigration(db, driver)
    migration.migrate()

    val size = db.withSession{implicit s =>
      lessonGrades.length.run
    }

    assert(0 == size)
  }

  test("migrate") {
    val userId = 5345
    val gradeValue = Some(435.45f)
    val comment = "message"
    val date = Some(DateTime.now.withMillisOfDay(0))

    val lessonId = db.withSession{implicit s =>
      val lessonId = addLesson()

      addPackageGrade(lessonId, userId, gradeValue, comment, date )

      lessons
    }

    val migration = new PackageGradeMigration(db, driver)
    migration.migrate()

    val grades = db.withSession{implicit s =>
      lessonGrades.list
    }

    assert(1 == grades.length)
    val g = grades.head
    assert(userId == g.userId)
    assert(gradeValue == g.grade)
    assert(comment == g.comment.get)
  }

  test("migrate 2") {
    val userId = 5345
    val gradeValue = None
    val comment = ""
    val date = None

    val lessonId = db.withSession{implicit s =>
      val lessonId = addLesson()

      addPackageGrade(lessonId, userId, gradeValue, comment, date )

      lessons
    }

    val migration = new PackageGradeMigration(db, driver)
    migration.migrate()

    val grades = db.withSession{implicit s =>
      lessonGrades.list
    }

    assert(1 == grades.length)
    val g = grades.head
    assert(userId == g.userId)
    assert(g.grade.isEmpty)
    assert(g.comment.isEmpty)
  }

  test("no more lesson test") {
    val userId = 5345
    val gradeValue = None
    val comment = ""
    val date = None

    db.withSession{implicit s =>
      addPackageGrade(3434, userId, gradeValue, comment, date )
    }

    val migration = new PackageGradeMigration(db, driver)
    migration.migrate()

    val grades = db.withSession{implicit s =>
      lessonGrades.list
    }

    assert(0 == grades.length)
  }

  private def addPackageGrade(lessonId: Long,
                              userId: Long,
                              grade: Option[Float],
                              comment: String,
                              date: Option[DateTime]
                             )(implicit s: JdbcBackend#Session): Unit = {
    val mapper = new JodaDateTimeMapper(driver.asInstanceOf[JdbcDriver]).typeMapper

    val dateValue = date.map(mapper.valueToSQLLiteral).getOrElse("NULL")
    val gradeValue = grade.map(_.toString).getOrElse("NULL")
    StaticQuery.updateNA(
      s"""insert into Learn_LFPackageGradeStorage
           (userId, packageId, grade, comment_, date_)
          	values ($userId, $lessonId, $gradeValue, '$comment', $dateValue);"""
    ).execute
  }
  private def addLesson(title: String = "l",
                        description: String = "lesson",
                        courseId: Long = courseId,
                        ownerId: Long = lessonOwnerId,
                        creationDate: DateTime = DateTime.now
                       )(implicit s: JdbcBackend#Session): Long = {
    lessons
      .map(x => (x.lessonType, x.title, x.description, x.courseId, x.ownerId, x.creationDate))
      .returning(lessons.map(_.id))
      .insert((LessonType.Tincan, title, description, courseId, ownerId, DateTime.now))
  }

}
