package com.arcusys.valamis.lesson.service.impl

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.lesson.model.{Lesson, UserLessonResult}
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader, UserLessonResultService}
import com.arcusys.valamis.lesson.storage.query.LessonAttemptsQueries
import com.arcusys.valamis.lesson.storage.{LessonAttemptsTableComponent, LessonTableComponent}
import com.arcusys.valamis.utils.TincanHelper
import com.arcusys.valamis.lrs.tincan.Statement
import TincanHelper.TincanAgent
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by lpotahina on 04.03.16.
  */
object UserLessonResultLock {
  val lock: AnyRef = new Object()
}

abstract class UserLessonResultServiceImpl(val db: JdbcBackend#DatabaseDef,
                                           val driver: JdbcProfile)
  extends UserLessonResultService
    with SlickProfile
    with LessonAttemptsQueries
    with LessonAttemptsTableComponent
    with LessonTableComponent {

  import driver.simple._

  def lessonService: LessonService

  def statementReader: LessonStatementReader

  def lessonResultCalculate: LessonResultCalculate

  override def get(lesson: Lesson, user: LUser): UserLessonResult = {
    val agent = user.getAgentByUuid

    val lessonAttempt = db.withSession { implicit s =>
      lessonAttempts.filterBy(lesson.id, user.getUserId).firstOption
    }

    lessonAttempt match {
      case Some(attempt) =>
        if (attempt.score.isEmpty && attempt.lastAttemptDate.isDefined) {
          db.withSession { implicit s =>
            lessonAttempts.filterBy(lesson.id, user.getUserId)
              .map(_.score)
              .update(statementReader.getLessonScoreMax(user.getAgentByUuid, lesson))
          }
        }
        attempt
      case None =>
        var isFinished = false
        val attempt = UserLessonResultLock.lock.synchronized {
          db.withSession { implicit s =>
            lessonAttempts.filterBy(lesson.id, user.getUserId).firstOption
          }.getOrElse {
            val newAttempt = lessonResultCalculate.calculateLessonResult(lesson, user.getUserId, agent)
            db.withSession { implicit s => lessonAttempts += newAttempt }
            isFinished = newAttempt.isFinished
            newAttempt
          }
        }
        if (isFinished) onLessonFinished(attempt)
        attempt
    }
  }

  def getAttemptedDate(lessonId: Long, userId: Long): Option[DateTime] = {
    db.withSession { implicit s =>
      lessonAttempts.filterBy(lessonId, userId).map(_.lastAttemptDate).firstFlatten
    }
  }

  def update(user: LUser, newStatements: Seq[Statement]): Unit = {

    val lessonToStatements = lessonResultCalculate.getLessonToStatements(newStatements)

    lessonToStatements.foreach { case (lessonIds, statements) =>
      lessonIds foreach { lessonId =>
        val newAttempt = lessonResultCalculate.calculateLessonResult(lessonId, user.getUserId, statements)
        val attemptQuery = lessonAttempts.filterBy(lessonId, user.getUserId)

        db.withSession { s => attemptQuery.firstOption(s) } match {
          case Some(attempt) =>
            val score = if (attempt.score.isEmpty && attempt.lastAttemptDate.isDefined) {
              val lesson = lessonService.getLessonRequired(attempt.lessonId)
              statementReader.getLessonScoreMax(user.getAgentByUuid, lesson)
            }
            else {
              attempt.score
            }
            db.withTransaction { implicit s =>
              attemptQuery.update(foldAttempts(attempt.copy(score = score), newAttempt))
            }
            if (!attempt.isFinished && newAttempt.isFinished) onLessonFinished(newAttempt)

          case None =>
            val state = calculateLessonResult(lessonId, user)
            db.withTransaction { implicit s =>
              lessonAttempts += state
            }
            if (newAttempt.isFinished) onLessonFinished(newAttempt)
        }
      }
    }
  }

  def onLessonFinished(lessonResult: UserLessonResult): Unit = {}

  private def foldAttempts(a: UserLessonResult, b: UserLessonResult): UserLessonResult = {
    a.copy(
      attemptsCount = a.attemptsCount + b.attemptsCount,
      lastAttemptDate = b.lastAttemptDate orElse a.lastAttemptDate,
      isSuspended = b.isSuspended,
      isFinished = a.isFinished || b.isFinished,
      score = a.score ++ b.score reduceOption Ordering.Float.max
    )
  }

  private def calculateLessonResult(lessonId: Long, user: LUser): UserLessonResult = {
    val lesson = lessonService.getLessonRequired(lessonId)
    val agent = user.getAgentByUuid
    lessonResultCalculate.calculateLessonResult(lesson, user.getUserId, agent)
  }

  override def isLessonAttempted(user: LUser, lesson: Lesson): Boolean = {
    get(lesson: Lesson, user: LUser).lastAttemptDate.isDefined
  }

  override def isLessonFinished(user: LUser, lesson: Lesson): Boolean = {
    get(lesson: Lesson, user: LUser).isFinished
  }

  override def getLastLessons(user: LUser, coursesIds: Seq[Long], count: Int): Seq[(UserLessonResult, Lesson)] = {
    val lessonsQ = lessons.filter(_.courseId inSet coursesIds)

    db.withSession { implicit s =>
      lessonAttempts
        .filter(a => a.lastAttemptDate.isDefined && a.userId === user.getUserId)
        .join(lessonsQ).on((a, l) => a.lessonId === l.id)
        .sortBy(_._1.lastAttemptDate.desc)
        .take(count)
        .list
    }

    //TODO: fill missing attempts
  }

  private def makeCourseAttemptsQuery(userId: Long, courseId: Long) = {
    val courseLessons = lessons.filter(_.courseId === courseId)

    lessonAttempts
      .filter(_.userId === userId)
      .join(courseLessons).on((a, l) => a.lessonId === l.id).map(_._1)
  }

  override def getUserResults(user: LUser, courseId: Long): Seq[UserLessonResult] = {
    val allLessons = lessonService.getAll(courseId)

    val results = db.withSession { implicit s =>
      makeCourseAttemptsQuery(user.getUserId, courseId).run
    }

    val newResults = allLessons
      .filterNot(lesson => results.exists(_.lessonId == lesson.id))
      .map(lesson => get(lesson, user))

    results ++ newResults
  }

  override def getLastResultForCourse(userId: Long, courseId: Long): Option[UserLessonResult] = {
    db.withSession { implicit s =>
      makeCourseAttemptsQuery(userId, courseId)
        .filter(_.lastAttemptDate.isDefined)
        .sortBy(_.lastAttemptDate desc)
        .firstOption
    }
  }

  override def getLastResult(userId: Long): Option[UserLessonResult] = {
    db.withSession { implicit s =>
      lessonAttempts
        .filter(_.userId === userId)
        .filter(_.lastAttemptDate.isDefined)
        .sortBy(_.lastAttemptDate desc)
        .firstOption
    }
  }

  override def get(users: Seq[LUser], lessons: Seq[Lesson]): Seq[UserLessonResult] = {
    if (users.isEmpty || lessons.isEmpty) {
      Nil
    } else {
      val lessonsIds = lessons.map(_.id)
      val usersIds = users.map(_.getUserId)

      val results = db.withSession { implicit s =>
        lessonAttempts
          .filterByUsersIds(usersIds)
          .filter(_.lessonId inSet lessonsIds)
          .list
      }

      for {
        user <- users
        lesson <- lessons
      } yield {
        results.find(r => r.userId == user.getUserId && r.lessonId == lesson.id)
          .getOrElse(get(lesson, user))
      }
    }
  }

  override def getAttemptedTotal(lessonsIds: Seq[Long],
                                 isFinished: Boolean,
                                 limit: Int,
                                 offset: Int): Seq[(Long, Int)] = db.withSession { implicit s =>
    val query = if (isFinished) {
      lessonAttempts.filter(_.isFinished)
    } else {
      lessonAttempts
    }

    query
      .filter(_.lessonId inSet lessonsIds)
      .filter(_.attemptsCount > 0)
      .groupBy(_.userId)
      .map { case (userId, group) =>
        if (isFinished) {
          (userId, group.length)
        } else {
          (userId, group.map(_.attemptsCount).sum.getOrElse(0))
        }
      }
      .sortBy {
        case (_, count) => count.desc
      }
      .drop(offset)
      .take(limit)
      .list
  }

  override def getAttemptedTotal(lessonsIds: Seq[Long],
                                 userIds: Seq[Long],
                                 isFinished: Boolean): Seq[(Long, Int)] = db.withSession { implicit s =>

    val query = if (isFinished) {
      lessonAttempts.filter(_.isFinished)
    } else {
      lessonAttempts
    }

    query
      .filter(_.lessonId inSet lessonsIds)
      .filter(_.userId inSet userIds)
      .filter(_.attemptsCount > 0)
      .groupBy(_.userId)
      .map { case (userId, group) =>
        if (isFinished) {
          (userId, group.length)
        } else {
          (userId, group.map(_.attemptsCount).sum.getOrElse(0))
        }
      }
      .list
  }
}
