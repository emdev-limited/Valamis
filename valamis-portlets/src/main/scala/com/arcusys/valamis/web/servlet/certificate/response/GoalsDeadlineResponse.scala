package com.arcusys.valamis.web.servlet.certificate.response

import org.joda.time.DateTime

case class GoalsDeadlineResponse(
  courses: Seq[CourseGoalDeadlineResponse],
  activities: Seq[ActivityGoalDeadlineResponse],
  statements: Seq[StatementGoalDeadlineResponse],
  packages: Seq[PackageGoalDeadlineResponse])

case class CourseGoalDeadlineResponse(id: Long, deadline: Option[DateTime])
case class ActivityGoalDeadlineResponse(name: String, deadline: Option[DateTime])
case class StatementGoalDeadlineResponse(obj: String, verb: String, deadline: Option[DateTime])
case class PackageGoalDeadlineResponse(packageId: Long, deadline: Option[DateTime])