package com.arcusys.valamis.web.servlet.report.response

case class CourseEventResponse(
  var enrollmentsCount: Int,
  var completionsCount: Int,
  groupName: String)
