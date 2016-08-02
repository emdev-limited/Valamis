package com.arcusys.valamis.web.servlet.certificate.facade

import com.arcusys.valamis.certificate.model.{CertificateFilter, CertificateStateFilter, CertificateStatuses}
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.model.SkipTake
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CertificateFacade(implicit val bindingModule: BindingModule)
  extends Injectable
  with CertificateFacadeContract
  with CertificateResponseFactory
  with CertificateGoals
  with CertificateUsers {


  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateStatusChecker = inject[CertificateStatusChecker]

  def getForSucceedUsers(companyId: Long, title: String, count: Option[Int] = None) = {
    val certificates = certificateRepository.getBy(
      new CertificateFilter(companyId, Some(title)),
      count.map(SkipTake(0, _))
    )

    certificates.flatMap(toCertificateSuccessUsersResponse)
  }

  def getStatesBy(userId: Long, companyId: Long, scopeId: Option[Long], statuses: Set[CertificateStatuses.Value]) = {
    val certificateStates = certificateStatusChecker.checkAndGetStatus(
      CertificateFilter(companyId, scope = scopeId, isPublished = Some(true)),
      CertificateStateFilter(Some(userId), statuses = statuses)
    )
    val certificateIds = certificateStates.map(_.certificateId).toSet

    val certificates = certificateRepository.getByIds(certificateIds)
        .map(c => c.id -> c).toMap

    certificateStates
      .map(s => toStateResponse(certificates(s.certificateId), s))
      .sortBy(_.title)
  }
}
