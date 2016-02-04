package com.arcusys.learn.models.response.users

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.models.response.certificates.CertificateResponseContract
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.model.goal.GoalStatistic
import com.arcusys.valamis.lrs.model.EndpointInfo

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
    picture = UserResponseUtils.getPortraitUrl(lUser),
    pageUrl = UserResponseUtils.getPublicUrl(lUser)
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

case class UserWithCertificatesResponse(id: Long,
                                        name: String,
                                        picture: String,
                                        pageUrl: String,
                                        certificates: Seq[CertificateResponseContract])

case class UserWithCertificateStatusResponse(id: Long,
                                             name: String,
                                             picture: String,
                                             pageUrl: String,
                                             date: String,
                                             status: CertificateStatuses.Value)  extends UserResponseBase