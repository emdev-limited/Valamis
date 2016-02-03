package com.arcusys.learn.models.response.certificates

import com.arcusys.valamis.lrs.tincan.LanguageMap

/**
 * Created by mminin on 03.03.15.
 */

case class GoalsStatusResponse(
  courses: Iterable[CourseStatusResponse],
  activities: Iterable[ActivityStatusResponse],
  statements: Iterable[StatementStatusResponse],
  packages: Iterable[PackageStatusResponse])

case class ActivityStatusResponse(activityId: String, status: String, dateFinish: String)

case class CourseStatusResponse(courseGoalId: Long, status: String, dateFinish: String)

case class StatementStatusResponse(
    tincanStmntObj: String,
    tincanStmntObjName: Option[LanguageMap],
    tincanStmntVerb: String,
    status: String,
    dateFinish: String
)

case class PackageStatusResponse(packageId: Long, status: String, dateFinish: String)