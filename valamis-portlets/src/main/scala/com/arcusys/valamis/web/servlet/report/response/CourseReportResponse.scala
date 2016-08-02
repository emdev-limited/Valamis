package com.arcusys.valamis.web.servlet.report.response

case class CourseReportResponse(id: Long,
  name: String,
  coursesCount: Int,
  var studentsCount: Int,
  var studentsStartedCount: Int,
  var studentsCompletedCount: Int,
  var studentsIncompletedCount: Int,
  var studentsUnknownCount: Int,
  var studentsPassedCount: Int,
  var studentsFailedCount: Int)
