package com.arcusys.learn.models.response.certificates

import com.arcusys.learn.models.CourseResponse

case class CertificateWithUserStatisticsResponse(
  id: Long,
  title: String,
  shortDescription: String,
  description: String,
  logo: String,
  isPublished: Boolean,
  totalUsers: Int,
  successUsers: Int,
  failedUsers: Int,
  overdueUsers: Int,
  scope: Option[CourseResponse]
)
