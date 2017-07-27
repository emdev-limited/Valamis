/*package com.arcusys.valamis.reports

import java.sql.Connection

import com.arcusys.valamis.lesson.model.{Lesson, LessonGrade, LessonType, UserLessonResult}
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader}
import com.arcusys.valamis.lrssupport.lrs.service.LrsClientManager
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.reports.service.ReportServiceImpl
import com.arcusys.valamis.reports.table.LessonTables
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class UserToLessonCountTest
  extends FunSuite
    with BeforeAndAfter
    with LessonTables
    with SlickProfile
    with SlickDbTestBase {

  import driver.api._

  before {
    createDB()
    val action = db.run((lessons.schema ++ lessonAttempts.schema ++ lessonGrades.schema).create)
    Await.result(action, Duration.Inf)
  }
  after {
    dropDB()
  }

  def reportService = new ReportServiceImpl(driver, db) {
    override def reader: LessonStatementReader = new LessonStatementReader() {
      override def lrsClient: LrsClientManager = ???
      override def lessonService: LessonService = ???
      override def isCompletedSuccess(userId: Long, lessonId: Long, after: DateTime, before: DateTime): Boolean = true
    }
  }

  val now = DateTime.now
  val start = now.minusDays(7)
  val end = now.withTimeAtStartOfDay().plusDays(1)
  val courseIds = Seq(20589L, 20655L)

  test("empty data") {
    val resultF = reportService.getCompletedLessonsByPeriod(start, end, courseIds)
    val result = Await.result(resultF, Duration.Inf)

    assert(result.isEmpty)
  }

  test("check in lesson attempts") {
    val resultF = createLesson(20589L, requiredReview = false, 0.7D) flatMap { lessonId =>
      Future.sequence(Seq(
        createLessonAttempt(lessonId, 1L, isFinished = true, score = Some(1F), now),
        createLessonAttempt(lessonId, 2L, isFinished = true, score = Some(1F), now.minusDays(10)),
        createLessonAttempt(lessonId, 3L, isFinished = false, score = Some(1F), now),
        createLessonAttempt(lessonId, 4L, isFinished = true, score = Some(0.6F), now)
      )) flatMap { attempt =>
        reportService.getCompletedLessonsByPeriod(start, end, courseIds)
      }
    }
    val result = Await.result(resultF, Duration.Inf)

    assert(result.get(1L).contains(1))
    assert(result.get(2L).contains(1))
    assert(result.get(3L).isEmpty)
    assert(result.get(4L).isEmpty)
  }

  test("check in lesson grades") {
    val resultF = createLesson(20655L, requiredReview = true, 0.7D) flatMap { lessonId =>
      Future.sequence(Seq(
        createLessonGrade(lessonId, 1L, grade = Some(1F), now),
        createLessonGrade(lessonId, 2L, grade = Some(1F), now.minusDays(10)),
        createLessonGrade(lessonId, 3L, grade = Some(1F), now.minusDays(1)),
        createLessonGrade(lessonId, 4L, grade = Some(0.6F), now)
      )) flatMap { attempt =>
        reportService.getCompletedLessonsByPeriod(start, end, courseIds)
      }
    }
    val result = Await.result(resultF, Duration.Inf)

    assert(result.get(1L).contains(1))
    assert(result.get(2L).isEmpty)
    assert(result.get(3L).contains(1))
    assert(result.get(4L).isEmpty)
  }

  test("check by courses") {
    val resultF = createLesson(2L, requiredReview = true, 0.7D) flatMap { lessonId =>
      Future.sequence(Seq(
        createLessonAttempt(lessonId, 1L, isFinished = true, score = Some(1F), now),
        createLessonAttempt(lessonId, 2L, isFinished = false, score = Some(1F), now)
      )) flatMap { attempt =>
        reportService.getCompletedLessonsByPeriod(start, end, courseIds)
      }
    }
    val result = Await.result(resultF, Duration.Inf)

    assert(result.get(1L).isEmpty)
    assert(result.get(2L).isEmpty)
  }


  private def createLesson(courseId: Long, requiredReview: Boolean, scoreLimit: Double) = {
    db.run(lessons returning lessons.map(_.id) += Lesson(
      -1L,
      LessonType.Tincan,
      "title",
      "description",
      logo = None,
      courseId = courseId,
      isVisible = Some(true),
      beginDate = None,
      endDate = None,
      ownerId = 20199L,
      creationDate = new DateTime,
      requiredReview = requiredReview,
      scoreLimit = scoreLimit)
    )
  }

  private def createLessonAttempt(lessonId: Long, userId: Long, isFinished: Boolean, score: Option[Float], date: DateTime) = {
    db.run(lessonAttempts += UserLessonResult(
      lessonId = lessonId,
      userId = userId,
      attemptsCount = 10,
      lastAttemptDate = Some(date),
      isSuspended = false,
      isFinished = isFinished,
      score = score)
    )

  }

  private def createLessonGrade(lessonId: Long, userId: Long, grade: Option[Float], date: DateTime) = {
    db.run(lessonGrades += LessonGrade(
      lessonId = lessonId,
      userId = userId,
      grade = grade,
      date = date,
      comment = None)
    )
  }

}*/
