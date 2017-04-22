package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lrs.model.StatementFilter
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.service.util.{StatementApiHelpers, TinCanVerbs, TincanHelper}
import com.arcusys.valamis.lrs.service.util.TinCanVerbs.VerbExtension
import com.arcusys.valamis.lrs.tincan.{Activity, Agent, Statement}
import StatementApiHelpers._
import TincanHelper.TincanAgent
import org.joda.time.DateTime

/**
 * Created by mminin on 22.09.15.
 */
abstract class LessonStatementReader {

  def lrsClient: LrsClientManager
  def lessonService: LessonService

  def getLastAttempted(userId: Long, lesson: Lesson): Option[Statement] = {
    val activityId = getActivityId(lesson)
    getAgent(userId).flatMap { agent =>
      getLastAttempted(agent, activityId)
    }
  }

  def getLastAttempted(user: LUser, lesson: Lesson): Option[Statement] = {
    val agent = getAgent(user)
    val activityId = lessonService.getRootActivityId(lesson)
    getLastAttempted(agent, activityId)
  }

  def getLastAttempted(user: LUser, activityId: String): Option[Statement] = {
    val agent = getAgent(user)
    getLastAttempted(agent, activityId)
  }

  def getLastAttempted(agent: Agent, activityId: String): Option[Statement] = {
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Attempted)),
      limit = Some(1)
    ))).headOption
  }

  def getAllAttempted(userId: Long, limit: Int = 0): Seq[Statement] = {
    getAgent(userId).map { agent =>
      lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Attempted)),
        limit = Some(limit)
      )))
    } getOrElse {
      Nil
    }
  }

  def getAttempted(user: LUser, activityId: String): Seq[Statement] = {
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(user.getAgentByUuid),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Attempted))
    )))
  }

  def getRelatedActivities(agent: Agent, activityId: String, limit: Int = 100): Seq[Statement] = {
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      relatedActivities = Some(true),
      limit = Some(limit)
    )))
  }

  def getCompleted(user: LUser, activityId: String): Seq[Statement] = {
    val agent = getAgent(user)
    getCompleted(agent, activityId)
  }

  def getExperienced(agent: Agent, activityId: String): Seq[Statement] = {
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.Experienced.toUriString),
      relatedActivities = Some(true)
    )))
  }

  def getCompleted(agent: Agent, activityId: String): Seq[Statement] = {
    val verbs = Seq(TinCanVerbs.Completed, TinCanVerbs.Passed)

    verbs flatMap { verb =>
      lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        activity = Some(activityId),
        verb = Some(TinCanVerbs.getVerbURI(verb))
      )))
    }
  }

  def getCompletedByLesson(lessonId: Long, after: DateTime, before: DateTime): Seq[Statement] = {
    val activityId = getActivityId(lessonId)

    val verbs = Seq(TinCanVerbs.Completed, TinCanVerbs.Passed)

    verbs flatMap { verb =>
      lrsClient.statementApi(_.getByFilter(StatementFilter(
        activity = Some(activityId),
        verb = Some(TinCanVerbs.getVerbURI(verb)),
        since = Some(after),
        until = Some(before)
      )))
    }
  }

  def getCompletedByUser(userId: Long, after: DateTime, before: DateTime): Seq[Statement] = {
    getAgent(userId).map { agent =>

      val completed = lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Completed)),
        since = Some(after),
        until = Some(before)
      )))

      val passed = lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Passed)),
        since = Some(after),
        until = Some(before)
      )))

      completed ++ passed
    } getOrElse {
      Nil
    }
  }

  def isCompletedSuccess(userId: Long, lessonId: Long, after: DateTime, before: DateTime): Boolean = {
    val activityId = getActivityId(lessonId)
    getAgent(userId).map { agent =>

      val completedStatement = lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        activity = Some(activityId),
        verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Completed)),
        since = Some(after),
        until = Some(before)
      )))

      lazy val passedStatement = lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        activity = Some(activityId),
        verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Passed)),
        since = Some(after),
        until = Some(before)
      )))

      hasFinished(completedStatement) || hasFinished(passedStatement)
    } getOrElse {
      false
    }
  }

  def getCompletedForAttempt(user: LUser, activityId: String, attemptedStatement: Statement): Option[Statement] = {
    val agent = getAgent(user)
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(attemptedStatement.id.get.toString),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Completed)),
      relatedActivities = Some(true),
      limit = Some(1)
    ))) headOption
  }

  def getAll(userId: Long, packageId: Long, limit: Int = 25): Seq[Statement] = {
    val activityId = getActivityId(packageId)
    getAgent(userId).map { agent =>

      lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        activity = Some(activityId),
        relatedActivities = Some(true),
        limit = Some(limit)
      )))
    } getOrElse {
      Nil
    }
  }

  def getAllAttempts(userId: Long, activityId: String, limit: Int = 25, offsest: Int = 0): Seq[Statement] = {
    getAgent(userId).map { agent =>
      lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        activity = Some(activityId),
        verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Attempted)),
        limit = Some(limit),
        offset = Some(offsest)
      )))
    } getOrElse {
      Nil
    }
  }

  def getByActivityId(userId: Long,
                      activityId: String,
                      verbs: Seq[String]): Seq[Statement] = {
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val agent = getAgent(userId)

    lrsClient.statementApi { api =>

      verbs
        .flatMap {
          // complete and passed statements have activityId
          case TinCanVerbs.Completed =>
            api.getByFilter(getStatementFilter(agent, activityId, TinCanVerbs.Completed))
          case TinCanVerbs.Passed =>
            api.getByFilter(getStatementFilter(agent, activityId, TinCanVerbs.Passed))
          case TinCanVerbs.Commented =>
            api.getByFilter(getStatementFilter(None, activityId, TinCanVerbs.Commented, related = true))
          // other statements have activityId in related statement
          case verb: String =>
            api.getByFilter(getStatementFilter(agent, activityId, verb, related = true))
        }
        .sortBy { statement =>
          statement.timestamp
        }

    }
  }

  private def getStatementFilter(agent: Option[Agent], activityId: String, verb: String, related: Boolean = false): StatementFilter = {
    StatementFilter(
      agent = agent,
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(verb)),
      relatedActivities = Some(related)
    )
  }

  def getAnsweredByPackageId(userId: Long, packageId: Long, limit: Int = 25): Seq[Statement] = {
    val activityId = getActivityId(packageId)
    val agent = getAgent(userId)

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = agent,
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Answered)),
      relatedActivities = Some(true),
      limit = Some(limit)
    )))
  }

  def getRoot(userId: Long, packageId: Long, limit: Int = 25): Seq[Statement] = {
    val activityId = getActivityId(packageId)
    val agent = getAgent(userId)

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = agent,
      activity = Some(activityId),
      limit = Some(limit)
    )))
  }

  def getLast(userId: Long, lesson: Lesson): Option[Statement] = {
    val activityId = getActivityId(lesson)
    val agent = getAgent(userId)

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = agent,
      activity = Some(activityId),
      relatedActivities = Some(true),
      limit = Some(1)
    ))) headOption
  }

  private def getActivityId(lessonId: Long): String = {
    lessonService.getRootActivityId(lessonId)
  }

  private def getActivityId(lesson: Lesson): String = {
    lessonService.getRootActivityId(lesson)
  }

  private def getAgent(userId: Long): Option[Agent] = {
    UserLocalServiceHelper().fetchUser(userId).map(_.getAgentByUuid)
  }

  private def getAgent(user: LUser): Agent = {
    user.getAgentByUuid
  }

  def getLessonScoreMax(agent: Agent, lesson: Lesson): Option[Float] = {
    val activityId = getActivityId(lesson)

    val verbs = Seq(TinCanVerbs.Completed, TinCanVerbs.Passed)

    lrsClient.statementApi { api =>
      verbs.flatMap { verb =>
        api.getByFilter(StatementFilter(
          agent = Some(agent),
          activity = Some(activityId),
          verb = Some(TinCanVerbs.getVerbURI(verb))
        ))
          .flatMap(s => s.result.flatMap(_.score).flatMap(_.scaled))
      }
        .reduceOption(Ordering.Float.max)
    }
  }

  def hasFinished(completedStatements: Seq[Statement]): Boolean = {
    completedStatements.exists { s =>
      s.result.exists(r => r.success.contains(true) || r.completion.contains(true))
    }
  }
}
