package com.arcusys.valamis.web.servlet.certificate.facade

import com.arcusys.valamis.certificate.model.CertificateFilter
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.model.SkipTake
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CertificateFacade(implicit val bindingModule: BindingModule)
  extends Injectable
  with CertificateFacadeContract
  with CertificateResponseFactory
  with CertificateGoals {


  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateStatusChecker = inject[CertificateStatusChecker]

  def getForSucceedUsers(companyId: Long, title: String, count: Option[Int] = None) = {
    val certificates = certificateRepository.getBy(
      new CertificateFilter(companyId, Some(title)),
      skipTake = count.map(SkipTake(0, _))
    )

    certificates.flatMap(toCertificateSuccessUsersResponse)
  }
}
