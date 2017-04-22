package com.arcusys.valamis.reports.service

import scala.concurrent.{ ExecutionContext, Future }

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import com.arcusys.valamis.reports.model._

abstract class ReportServiceImpl(val driver: JdbcProfile, val db: JdbcBackend#Database)
    extends ReportService
    with TopLessonReporting {

  def getTopLessons(cfg: TopLessonConfig)(implicit ec: ExecutionContext): Future[LessonReport] = {
    getLessonsByAttempt(cfg) zip getLessonsByGrade(cfg) map {
      case (attempts, grades) =>
        (grades ++ attempts) sortBy (- _._4) take cfg.limit map TopLesson.tupled
    } map LessonReport
  }

  def getUsersToLessonCount(since: DateTime, until: DateTime, courseIds: Seq[Long])
                           (implicit ec: ExecutionContext): Future[Map[Long, Int]] = {
    val attemptsF = getUserAttempts(since, until, courseIds)
    val gradesF = getUserGrades(courseIds, since, until)

    attemptsF zip gradesF map {
      case (attempts, grades) => attempts ++ grades
    }
  }
}
