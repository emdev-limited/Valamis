package com.arcusys.valamis.reports.service

import scala.concurrent.{ ExecutionContext, Future }

import com.arcusys.valamis.reports.model._

trait ReportService {
  def getTopLessons(cfg: TopLessonConfig)(implicit ec: ExecutionContext): Future[LessonReport]

  def getUsersToLessonCount(since: DateTime, until: DateTime, courseIds: Seq[Long])
                           (implicit ec: ExecutionContext): Future[Map[Long, Int]]
}
