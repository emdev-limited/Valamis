package com.arcusys.valamis.web.servlet.certificate.facade

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.model.goal.GoalStatuses
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.web.servlet.certificate.response._
import com.arcusys.valamis.web.servlet.course.CourseFacadeContract
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

@deprecated
trait CertificateGoals extends Injectable with CertificateFacadeContract with CertificateResponseFactory {

  private lazy val certificateChecker = inject[CertificateStatusChecker]
  private lazy val lrsReader = inject[LrsClientManager]
  private lazy val courseFacade = inject[CourseFacadeContract]

  def getGoalsStatuses(certificateId: Long, userId: Long): GoalsStatusResponse = {
    val user = UserLocalServiceHelper().getUser(userId)
    val format = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("SS", user.getLocale))
      .withZone(DateTimeZone.forTimeZone(user.getTimeZone))

    val courses = certificateChecker.getCourseGoalsStatus(certificateId, userId)
    val activities = certificateChecker.getActivityGoalsStatus(certificateId, userId)
    val statements = certificateChecker.getStatementGoalsStatus(certificateId, userId)
    val packagesGoalsStatus = certificateChecker.getPackageGoalsStatus(certificateId, userId)
    val assignments = certificateChecker.getAssignmentGoalsStatus(certificateId, userId)

    def dateToString(date: Option[DateTime]) = date.map(d => format.print(d)).getOrElse("")

    //TODO delete taking name in lrs
    def getObjName(activityId: String) =
    lrsReader
      .activityApi(_.getActivity(activityId))
      .toOption
      .flatMap(_.name)
    lazy val lessonService = inject[LessonService]

    GoalsStatusResponse(
      courses.map { x =>
        val courseTitle = courseFacade.getCourse(x.goal.courseId)
          .map(_.title)
          .getOrElse("")
        CourseStatusResponse(x.goal.goalId,
          x.goal.courseId,
          x.status.toString,
          dateToString(x.finishDate),
          courseTitle)
      },
      activities.map { x => ActivityStatusResponse(x.goal.goalId,
        x.goal.activityName,
        x.status.toString,
        dateToString(x.finishDate),
        LiferayActivity.activities
          .filter(_.activityId == x.goal.activityName)
          .map(_.title).headOption.getOrElse("")
      )
      },
      statements.map(x => StatementStatusResponse(x.goal.goalId, x.goal.obj, getObjName(x.goal.obj),
        x.goal.verb,
        x.status.toString,
        dateToString(x.finishDate))),
      packagesGoalsStatus.map { x =>
        val lessonTitle = lessonService.getLesson(x.goal.packageId).map(_.title).getOrElse("")
        PackageStatusResponse(x.goal.goalId,
          x.goal.packageId,
          x.status.toString,
          dateToString(x.finishDate),
          lessonTitle)
      },
      assignments.map(x => AssignmentStatusResponse(x.goal.goalId,
        x.goal.assignmentId,
        x.status.toString,
        dateToString(x.finishDate)))
    )
  }

  def getCountGoals(certificateId: Long, userId: Long): Int = {
    val courses = certificateChecker.getCourseGoalsStatus(certificateId, userId)
    val activities = certificateChecker.getActivityGoalsStatus(certificateId, userId)
    val statements = certificateChecker.getStatementGoalsStatus(certificateId, userId)
    val packagesGoalsStatus = certificateChecker.getPackageGoalsStatus(certificateId, userId)

    val sc = courses.count(_.status == GoalStatuses.Success)
    val sa = activities.count(_.status == GoalStatuses.Success)
    val ss = statements.count(_.status == GoalStatuses.Success)
    val sp = packagesGoalsStatus.count(_.status == GoalStatuses.Success)

    sc + sa + ss + sp
  }

  def getGoalsDeadlines(certificateId: Long, userId: Long) = GoalsDeadlineResponse(
    certificateChecker.getCourseGoalsDeadline(certificateId, userId)
      .map(d => CourseGoalDeadlineResponse(d.goal.courseId, d.deadline)),
    certificateChecker.getActivityGoalsDeadline(certificateId, userId)
      .map(d => ActivityGoalDeadlineResponse(d.goal.activityName, d.deadline)),
    certificateChecker.getStatementGoalsDeadline(certificateId, userId)
      .map(d => StatementGoalDeadlineResponse(d.goal.obj, d.goal.verb, d.deadline)),
    certificateChecker.getPackageGoalsDeadline(certificateId, userId)
      .map(d => PackageGoalDeadlineResponse(d.goal.packageId, d.deadline))
  )
}
