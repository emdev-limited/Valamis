package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.web.servlet.course.CourseResponse

case class CertificateWithUserStatisticsResponse(id: Long,
                                                 title: String,
                                                 shortDescription: String,
                                                 description: String,
                                                 logo: String,
                                                 isActive: Boolean,
                                                 totalUsers: Int,
                                                 successUsers: Int,
                                                 failedUsers: Int,
                                                 overdueUsers: Int,
                                                 scope: Option[CourseResponse]
)