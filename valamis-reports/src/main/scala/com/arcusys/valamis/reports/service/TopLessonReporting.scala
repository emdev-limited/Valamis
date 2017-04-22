package com.arcusys.valamis.reports.service

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import com.arcusys.learn.liferay.services.{CompanyHelper, UserLocalServiceHelper => UserHelper}
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader}
import com.arcusys.valamis.lesson.storage.query.{LessonAttemptsQueries, LessonGradesQueries, LessonQueries}
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import com.arcusys.valamis.reports.table.LessonTables

import com.arcusys.valamis.reports.model._


trait TopLessonReporting
    extends LessonTables
    with LessonQueries
    with LessonAttemptsQueries
    with LessonGradesQueries {

  val driver: JdbcProfile
  val db: JdbcBackend#Database

  def reader: LessonStatementReader
  def lessonService: LessonService

  private type LessonSample = (Long, String, Option[String], Int)

  import driver.api._

  def getLessonsByGrade(cfg: TopLessonConfig)(implicit ec: ExecutionContext): Future[Seq[LessonSample]] = {
    val lq = lessons.filterByCourseIds(cfg.courseIds)
    val gq = lessonGrades.filterByUsersIds(cfg.userIds)
    val q = lessonGradesQ(lq, gq, cfg.since, cfg.until)

    val action = q map {
      case (l, g) =>
        (l.id, l.title, l.logo, g.userId)
    } result

    val shaped = action map { grades =>
      grades groupBy (g => (g._1, g._2, g._3)) map {
        case (g, r) => (g._1, g._2, g._3, r.length)
      } toSeq
    }

    db.run(shaped)
  }

  def getLessonsByAttempt(cfg: TopLessonConfig)(implicit ec: ExecutionContext): Future[Seq[LessonSample]] = {
    val lq = lessons.filterByCourseIds(cfg.courseIds)
    val aq = lessonAttempts.filterByUsersIds(cfg.userIds)
    val q = lessonAttemptsQ(lq, aq)

    val action = q map {
      case (l, a) =>
        (l.id, l.title, l.logo, a.userId)
    } result

    val attemptsA = action map { attempts =>
      attempts groupBy (l => (l._1, l._2, l._3)) map {
        case (l, r) => (l._1, l._2, l._3, r map (_._4))
      } toSeq
    }

    // the company id doesn't get propagated through context switching
    val companyId = CompanyHelper.getCompanyId

    db.run(attemptsA) map { as =>
      CompanyHelper.setCompanyId(companyId)
      as map { a =>
        val userIds = filterUsers(a._1, a._4, cfg.since, cfg.until)
        (a._1, a._2, a._3, userIds.size)
      }
    }
  }

  def getUserAttempts(since: DateTime, until: DateTime, courseIds: Seq[Long])
                     (implicit ec: ExecutionContext): Future[Map[Long, Int]] = {
    val companyId = CompanyHelper.getCompanyId

    val lq = lessons.filterByCourseIds(courseIds)
    val q = lessonAttemptsQ(lq, lessonAttempts)

    val action = q map {
      case (l, a) => (a.userId, l.id)
    } result

    db.run(action) map { data =>
      CompanyHelper.setCompanyId(companyId)
      data groupBy (_._1) map {
        case (userId, group) =>
          val lessonIds = filterLessons(userId, group map (_._2), since, until)
          (userId, lessonIds.size)
      }
    }
  }

  def getUserGrades(courseIds: Seq[Long], since: DateTime, until: DateTime)
                   (implicit ec: ExecutionContext): Future[Map[Long, Int]] = {
    val lq = lessons.filterByCourseIds(courseIds)
    val q = lessonGradesQ(lq, lessonGrades, since, until)

    val action = q map {
      case (l, g) => (g.userId, l.id)
    } result

    db.run(action) map { data =>
      data groupBy (_._1) map {
        case (userId, grouped) =>
          (userId, grouped.size)
      }
    }
  }

  private def lessonGradesQ(lessonQ: LessonQuery, gradeQ: LessonGradesQuery, since: DateTime, until: DateTime) = {
    lessonQ join gradeQ on {
      _.id === _.lessonId
    } filter {
      case (l, g) =>
        l.scoreLimit <= (g.grade.asColumnOf[Double] + 0.001) &&
          g.date >= since &&
          g.date <= until
    }
  }

  private def lessonAttemptsQ(lessonQ: LessonQuery, attemptQ: LessonAttemptsQuery) = {
    lessonQ join attemptQ on {
      _.id === _.lessonId
    } filter {
      case (l, a) =>
        l.requiredReview === false &&
          l.scoreLimit <= a.score.asColumnOf[Double] &&
          a.isFinished === true
    }
  }

  private def filterUsers(lessonId: Long, userIds: Seq[Long], since: DateTime, until: DateTime) = {
    val sts = reader.getCompletedByLesson(lessonId, since, until)

    val userHashes = userIds map (id => (userHash(id), id)) toMap
    val agentHashes = extractAgentsHash(sts)

    filterIn(agentHashes, userHashes)
  }

  private def filterLessons(userId: Long, lessonIds: Seq[Long], since: DateTime, until: DateTime) = {
    val sts = reader.getCompletedByUser(userId, since, until)

    val activityHash = lessonIds map (id => (getActivityId(id), id)) toMap
    val activities = extractActivities(sts)

    filterIn(activities, activityHash)
  }

  private def filterIn[A,B](targets: Seq[A], amid: Map[A, B]): Seq[B] = {
    targets.foldLeft(Seq[B]()) {
      (filtered, target) =>
        val f = amid.get(target)
        if (f.isDefined) f.get +: filtered else filtered
    }
  }

  private def userHash(userId: Long) = {
    UserHelper().getUser(userId).getUserUuid
  }

  private def extractAgentsHash(sts: Seq[Statement]) = sts collect {
    case s if s.actor.account.isDefined =>
      s.actor.account.get.name
  }

  private def extractActivities(sts: Seq[Statement]) = sts collect {
    case s if s.obj.isInstanceOf[Activity] =>
      s.obj.asInstanceOf[Activity].id
  }

  private def getActivityId(lessonId: Long) = {
    lessonService.getRootActivityId(lessonId)
  }
}
