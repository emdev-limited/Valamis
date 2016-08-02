package com.arcusys.valamis.lesson.service.impl

import com.arcusys.valamis.lesson.model.{UserLessonResult, Lesson}
import com.arcusys.valamis.lesson.service.{LessonStatementReader, LessonService}
import com.arcusys.valamis.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.lrs.tincan.{Activity, Statement, Agent}

abstract class LessonResultCalculate {

  val attemptedVerb = TinCanVerbs.getVerbURI(TinCanVerbs.Attempted)
  val completedVerbs = Seq(TinCanVerbs.Completed, TinCanVerbs.Passed)
    .map(TinCanVerbs.getVerbURI)

  def lessonService: LessonService
  def statementReader: LessonStatementReader

  def calculateLessonResult(lesson: Lesson, userId: Long, agent: Agent): UserLessonResult = {
    val activityId = lessonService.getRootActivityId(lesson)
    val completeStatements = statementReader.getCompleted(agent, activityId)

    val attemptsCount = completeStatements.size
    val isFinished = hasFinished(completeStatements)

    val lastAttemptDate = completeStatements.headOption.map(_.timestamp)

    val isSuspended = !isAttemptCompleted(
      statementReader.getLastAttempted(agent, activityId),
      completeStatements
    )

    val score = statementReader.getLessonScoreMax(agent, lesson)

    UserLessonResult(lesson.id, userId, attemptsCount, lastAttemptDate, isSuspended, isFinished, score)
  }

  def calculateLessonResult(lessonId: Long,
                                    userId: Long,
                                    statements: Seq[Statement]): UserLessonResult = {

    val completed = statements.filter(s => completedVerbs.contains(s.verb.id))
    val attempted = statements.filter(s => s.verb.id == attemptedVerb)

    val attemptsCount = completed.size
    val isFinished = hasFinished(completed)

    val lastAttemptDate = completed.headOption.map(_.timestamp)

    val isSuspended = !isAttemptCompleted(attempted.headOption, completed)

    val score = statements.flatMap(s => s.result.flatMap(_.score).flatMap(_.scaled)) reduceOption Ordering.Float.max

    UserLessonResult(lessonId, userId, attemptsCount, lastAttemptDate, isSuspended, isFinished, score)
  }

  def getLessonToStatements(newStatements: Seq[Statement]): Map[Option[Long], Seq[Statement]] = {
    newStatements
      .filter { s =>
        s.obj.isInstanceOf[Activity] && (s.verb.id == attemptedVerb || completedVerbs.contains(s.verb.id))
      }
      .groupBy { s => s.obj.asInstanceOf[Activity].id }
      .map { case (id, s) => (lessonService.getByRootActivityId(id), s) }
      .filterKeys { s => s.isDefined }
  }

  private def hasFinished(completedStatements: Seq[Statement]): Boolean = {
    completedStatements.exists { s =>
      s.result.exists(r => r.success.contains(true) || r.completion.contains(true))
    }
  }

  private def isAttemptCompleted(attemptStatement: Option[Statement],
                                 completedStatements: Seq[Statement]): Boolean = {
    attemptStatement.flatMap(_.id) match {
      case None => true
      case Some(id) => completedStatements
        .flatMap(_.context)
        .flatMap(_.contextActivities)
        .flatMap(_.groupingIds)
        .contains(id.toString)
    }
  }

}
