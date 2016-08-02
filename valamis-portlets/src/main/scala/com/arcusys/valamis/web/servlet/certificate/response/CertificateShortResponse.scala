package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.web.servlet.course.CourseResponse

case class CertificateShortResponse(
  id: Long,
  title: String,
  shortDescription: String,
  description: String,
  logo: String,
  isPublished: Boolean,
  courseCount: Int,
  statementCount: Int,
  activityCount: Int,
  packageCount: Int,
  assignmentCount: Int,
  userCount: Int,
  scope: Option[CourseResponse]) extends CertificateResponseContract