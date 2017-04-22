package com.arcusys.valamis.web.servlet.certificate.facade

import com.arcusys.valamis.web.servlet.certificate.response._

@deprecated
trait CertificateFacadeContract {

  def getGoalsStatuses(certificateId: Long, userId: Long): GoalsStatusResponse

  def getCountGoals(certificateId: Long, userId: Long): Int

  def getGoalsDeadlines(certificateId: Long, userId: Long): GoalsDeadlineResponse

  def getForSucceedUsers(companyId: Long, title: String, count: Option[Int] = None): Seq[CertificateSuccessUsersResponse]
}
