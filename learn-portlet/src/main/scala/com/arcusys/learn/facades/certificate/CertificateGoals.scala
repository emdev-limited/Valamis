package com.arcusys.learn.facades.certificate

import com.arcusys.learn.facades.CertificateFacadeContract
import com.arcusys.learn.models.response.certificates._
import com.arcusys.valamis.certificate.model.goal.GoalStatuses
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portal.service.UserLocalServiceUtil
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

trait CertificateGoals extends Injectable with CertificateFacadeContract with CertificateResponseFactory {

  private lazy val certificateChecker = inject[CertificateStatusChecker]
  private lazy val lrsReader = inject[LrsClientManager]

  def getGoalsStatuses(certificateId: Long, userId: Long): GoalsStatusResponse = {
    val user = UserLocalServiceUtil.getUserById(userId)
    val format = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("SS", user.getLocale))
      .withZone(DateTimeZone.forTimeZone(user.getTimeZone))

    val courses = certificateChecker.getCourseGoalsStatus(certificateId, userId)
    val activities = certificateChecker.getActivityGoalsStatus(certificateId, userId)
    val statements = certificateChecker.getStatementGoalsStatus(certificateId, userId)
    val packagesGoalsStatus = certificateChecker.getPackageGoalsStatus(certificateId, userId)

    def dateToString(date: Option[DateTime]) = date.map(d => format.print(d)).getOrElse("")

    def getObjName(activityId: String) =
      lrsReader
        .activityApi(_.getActivity(activityId))
        .toOption
        .flatMap(_.name)

    GoalsStatusResponse(
      courses.map(x => CourseStatusResponse(x.goal.courseId, x.status.toString, dateToString(x.finishDate))),
      activities.map(x => ActivityStatusResponse(x.goal.activityName, x.status.toString, dateToString(x.finishDate))),
      statements.map(x => StatementStatusResponse(x.goal.obj, getObjName(x.goal.obj), x.goal.verb, x.status.toString, dateToString(x.finishDate))),
      packagesGoalsStatus.map(x => PackageStatusResponse(x.goal.packageId, x.status.toString, dateToString(x.finishDate)))
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
