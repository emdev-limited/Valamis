package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.model.goal.GoalStatistic
import com.arcusys.valamis.gradebook.model.{CourseGrade, Statistic}
import com.arcusys.valamis.lrs.model.EndpointInfo
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.user.util.UserExtension
import com.arcusys.valamis.web.servlet.course.CourseResponse
import org.joda.time.DateTime

trait UserResponseBase{
  def id: Long
  def name: String
  def picture: String
  def pageUrl: String
}

case class UserResponse(id: Long,
                        name: String,
                        email: String,
                        picture: String = "",
                        pageUrl: String = ""
                         ) extends UserResponseBase {
  def this(lUser: LUser) = this(
    id = lUser.getUserId,
    name = lUser.getFullName,
    email = lUser.getEmailAddress,
    picture = lUser.getPortraitUrl,
    pageUrl = lUser.getPublicUrl
  )
}

case class UserWithEndpointResponse(id: Long,
                                    name: String,
                                    picture: String,
                                    pageUrl: String,
                                    endpointInfo: Option[EndpointInfo])

case class UserWithCertificateStatResponse(id: Long,
                                           name: String,
                                           picture: String,
                                           pageUrl: String,
                                           statistic: GoalStatistic,
                                           status: Option[CertificateStatuses.Value] = None
                                            ) extends UserResponseBase

case class UserWithCourseStatisticResponse(user: UserInfo,
                                           lastActivityDate: Option[DateTime],
                                           teacherGrade: Option[CourseGrade],
                                           lessons: Statistic,
                                           courses: Option[Statistic],
                                           course: Option[CourseResponse])


case class UserWithCertificateStatusResponse(id: Long,
                                             name: String,
                                             picture: String,
                                             pageUrl: String,
                                             date: String,
                                             status: CertificateStatuses.Value)  extends UserResponseBase