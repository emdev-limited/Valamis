package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.course.CourseGradeTableComponent
import com.arcusys.learn.liferay.update.version300.migrations.CourseGradeMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{H2Driver, JdbcProfile}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CourseGradeMigrationTest (val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with SlickProfile
    with CourseGradeTableComponent {

  def this() {
    this(H2Driver)
  }

  import driver.api._

  val db = Database.forURL("jdbc:h2:mem:courseGrade", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
  }
  after {
    connection.close()
  }

  private def createOldTable = {
    sqlu"""create table Learn_LFCourse(
        id_ LONG not null primary key,
        courseID INTEGER null,
        userID INTEGER null,
        grade VARCHAR(3000) null,
        comment_ TEXT null,
        date_ DATE null)"""
  }

  private def insertData(id: Long,
                         courseId: Int,
                         userId: Int,
                         grade: String,
                         comment: String) = {
    sqlu"""insert into Learn_LFCourse
          ( id_, courseID, userID, grade, comment_)
          values ( $id, $courseId, $userId, $grade, $comment)"""
  }

  test("migrate course grade") {

    Await.result(db.run {createOldTable >> insertData(1L, 21289, 20199, "50", "my grade")}, Duration.Inf)

    new CourseGradeMigration(db, driver).migrate()

    val data = Await.result(db.run {courseGrades.result}, Duration.Inf)

    assert(data.size == 1)

    assert(data.head.courseId == 21289)
    assert((data.head.grade.get - 0.5).abs < 0.1)
    assert(data.head.comment.contains("my grade"))
  }

  test("migrate course grade with empty grade") {

    Await.result(db.run {createOldTable >> insertData(1L, 21289, 20199, null, null)}, Duration.Inf)

    new CourseGradeMigration(db, driver).migrate()

    val data = Await.result(db.run {courseGrades.result}, Duration.Inf)

    assert(data.size == 1)

    assert(data.head.userId == 20199)
    assert(data.head.grade.isEmpty)
  }

}
