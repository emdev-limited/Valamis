package com.arcusys.valamis.reports.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.lesson.model.{Lesson, LessonExt, LessonStates, LessonType}
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lesson.tincan.model.TincanActivity
import com.arcusys.valamis.lesson.tincan.service.TincanPackageService
import com.arcusys.valamis.lrs.service.util.TinCanActivityType
import com.arcusys.valamis.lrs.service.util.TincanHelper.TincanAgent
import com.arcusys.valamis.lrs.tincan.{Activity, Statement, StatementObject}
import com.arcusys.valamis.reports.model.{ActivitiesStatuses, ActivityStatus, LessonStatus, PatternReportStatus}
import org.joda.time.DateTime

trait LearningPatternReportService {
  def getLessons(courseId: Long): Seq[Lesson]

  def getStatuses(courseId: Long, users: Seq[LUser]): Seq[LessonStatus]

  def getStatusesTotal(courseId: Long, users: Seq[LUser]): Seq[(Lesson, Map[PatternReportStatus.Value, Int])]

  def getLessonActivitiesStatuses(lessonId: Long, users: Seq[LUser]): Seq[ActivitiesStatuses]

  def getLessonActivitiesStatusesTotal(lessonId: Long, users: Seq[LUser]): Seq[(TincanActivity, Map[PatternReportStatus.Value, Int])]
}


abstract class LearningPatternReportServiceImpl extends LearningPatternReportService {
  val lessonService: LessonService
  val userResult: UserLessonResultService
  val teacherGradeService: TeacherLessonGradeService
  val lessonPlayerService: LessonPlayerService
  val tincanLessonService: TincanPackageService
  val statementReader: LessonStatementReader

  def getStatuses(courseId: Long, users: Seq[LUser]): Seq[LessonStatus] = {
    val lessons = getLessons(courseId)

    users.flatMap { user =>
      lessons.map { l =>
        val (status, date) = getLessonStatus(l, user)
        LessonStatus(l.id, user.getUserId, status, date)
      }
    }
  }

  def getLessons(courseId: Long): Seq[Lesson] = {
    lessonService.getAllVisible(courseId, extraVisible = true)
      .filter(_.lessonType == LessonType.Tincan)
      .sortBy(_.title)
  }

  private def getLessonStatus(lesson: Lesson, user: LUser): (PatternReportStatus.Value, Option[DateTime]) = {

    assert(lesson.lessonType == LessonType.Tincan)

    val lessonResult = userResult.get(lesson, user)
    val teacherGrade = teacherGradeService.get(user.getUserId, lesson.id)

    val grade = teacherGrade.flatMap(_.grade)

    val status = lesson.getLessonStatus(lessonResult, grade) match {
      case Some(LessonStates.Finished) =>
        PatternReportStatus.Finished

      case _ if (grade contains 0) || (lessonResult.score contains 0) =>
        PatternReportStatus.Failed

      case Some(LessonStates.Attempted) | Some(LessonStates.InReview) =>
        PatternReportStatus.Attempted

      case Some(LessonStates.Suspended) =>
        PatternReportStatus.Paused

      case _ if lessonPlayerService.isLessonVisible(user, lesson) =>
        PatternReportStatus.NotStarted

      case _ =>
        PatternReportStatus.Empty
    }

    (status, lessonResult.lastAttemptDate)
  }

  def getStatusesTotal(courseId: Long, users: Seq[LUser]): Seq[(Lesson, Map[PatternReportStatus.Value, Int])] = {
    val lessons = getLessons(courseId)

    lessons.map { l =>
      val statusToCount = users
        .map(getLessonStatus(l, _)._1)
        .groupBy(identity).mapValues(_.size)

      (l, statusToCount)
    }
  }

  def getLessonActivitiesStatuses(lessonId: Long, users: Seq[LUser]): Seq[ActivitiesStatuses] = {
    val lesson = lessonService.getLesson(lessonId).get

    val lessonActivityId = lessonService.getRootActivityId(lesson)
    val activities = getLessonActivities(lesson)

    users.map(getActivitiesStatuses(lesson, lessonActivityId, activities, _))
  }

  def getLessonActivitiesStatusesTotal(lessonId: Long, users: Seq[LUser]): Seq[(TincanActivity, Map[PatternReportStatus.Value, Int])] = {
    val lesson = lessonService.getLesson(lessonId).get

    val lessonActivityId = lessonService.getRootActivityId(lesson)
    val activities = getLessonActivities(lesson)

    users.map(getActivitiesStatuses(lesson, lessonActivityId, activities, _))
      .flatMap(_.statuses)
      .groupBy(_.activity)
      .mapValues(_.groupBy(_.status).mapValues(_.size))
      .toSeq
  }

  private def getLessonActivities(lesson: Lesson): Seq[TincanActivity] = {
    val interactionActivityTypes = Seq(TinCanActivityType.module, TinCanActivityType.cmiInteraction)
      .map(_.toString)

    tincanLessonService.getActivities(lesson.id)
      .filter(a => interactionActivityTypes.contains(a.activityType))
      //TODO: improve sorting
      // we should show activities in same order like on original file
      // but we do not store this information now
      .sortBy(_.id)
  }

  private def getActivitiesStatuses(lesson: Lesson,
                                    lessonActivityId: String,
                                    interactionActivities: Seq[TincanActivity],
                                    user: LUser): ActivitiesStatuses = {

    val (attemptStatement, statements) = getLastAttemptStatements(user, lessonActivityId)

    val statuses = if (attemptStatement.isEmpty && !lessonPlayerService.isLessonVisible(user, lesson)) {
      //user did not start the lesson and have no rights to start
      //status should be empty
      interactionActivities.map { a =>
        ActivityStatus(a, PatternReportStatus.Empty, date = None)
      }
    } else {
      interactionActivities.map { a =>
        getActivityStatus(a, statements)
      }
    }

    ActivitiesStatuses(
      user.getUserId,
      lesson.id,
      attemptStatement.flatMap(_.context).flatMap(_.revision),
      attemptStatement.map(_.stored),
      statuses
    )
  }

  private def getActivityStatus(activity: TincanActivity,
                                statements: Seq[Statement]): ActivityStatus = {
    val activityStatements = statements.filter(_.obj.activityId contains activity.activityId)

    val (status, date) = activityStatements match {
      case Nil =>
        (PatternReportStatus.NotStarted, None)

      case activityStatements: Seq[Statement] =>
        val isFailed = activityStatements
          .flatMap(_.result)
          .flatMap(_.success)
          .contains(false)

        val status = if (isFailed) PatternReportStatus.Failed else PatternReportStatus.Finished
        val date = activityStatements.map(_.timestamp).headOption

        (status, date)
    }

    ActivityStatus(activity, status, date)
  }

  private def getLastAttemptStatements(user: LUser,
                                       lessonActivityId: String
                                      ): (Option[Statement], Seq[Statement]) = {
    val agent = user.getAgentByUuid

    val lastAttemptStatement = statementReader.getLastAttempted(agent, lessonActivityId)

    val statements = lastAttemptStatement.toSeq
      .flatMap(_.obj.activityId)
      .flatMap(statementReader.getRelatedActivities(agent, _))

    (lastAttemptStatement, statements)
  }

  private implicit class StatementObjectExtensions(val obj: StatementObject) {
    def activityId: Option[String] = {
      Some(obj)
        .filter(_.isActivity)
        .map(_.asInstanceOf[Activity].id)
    }

    def isActivity: Boolean = obj.isInstanceOf[Activity]
  }

}
