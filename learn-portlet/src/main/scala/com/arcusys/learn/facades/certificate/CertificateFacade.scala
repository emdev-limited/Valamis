package com.arcusys.learn.facades.certificate

import java.io.{File, InputStream}

import com.arcusys.learn.facades.CertificateFacadeContract
import com.arcusys.learn.models.response.certificates.{CertificateResponse, _}
import com.arcusys.valamis.certificate.model.{CertificateFilter, CertificateStateFilter, CertificateStatuses}
import com.arcusys.valamis.certificate.service.export.{CertificateExportProcessor, CertificateImportProcessor}
import com.arcusys.valamis.certificate.service.{CertificateService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage.{CertificateRepository, CertificateStateRepository}
import com.arcusys.valamis.model.SkipTake
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CertificateFacade(implicit val bindingModule: BindingModule)
  extends Injectable
  with CertificateFacadeContract
  with CertificateResponseFactory
  with CertificateGoals
  with CertificateUsers {


  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateService = inject[CertificateService]
  private lazy val certificateStateRepository = inject[CertificateStateRepository]
  private lazy val certificateStatusChecker = inject[CertificateStatusChecker]

  def create(companyId: Long, ownerId: Long, title: String, description: String): CertificateResponse = {
    val c = certificateService.create(companyId, title, description)
    toCertificateResponse(c)
  }

  def getById(certificateId: Long): CertificateResponse = {
    val c = certificateRepository.getById(certificateId)
    toCertificateResponse(c)
  }

  def getById(certificateId: Long, userId: Long): CertificateResponse = {
    val c = certificateRepository.getById(certificateId)
    certificateStateRepository.getBy(userId, certificateId) match {
      case Some(_) => toCertificateResponse(c).copy(isJoint = Option(true))
      case None => toCertificateResponse(c).copy(isJoint = Option(false))
    }
  }

  def getForSucceedUsers(companyId: Long, title: String, count: Option[Int] = None) = {
    val certificates = certificateRepository.getBy(
      new CertificateFilter(companyId, Some(title)),
      count.map(SkipTake(0, _))
    )

    certificates.flatMap(toCertificateSuccessUsersResponse)
  }

  def change(id: Long, title: String, description: String, validPeriodType: String, validPeriodValue: Option[Int], isOpenBadgesIntegration: Boolean,
    shortDescription: String = "", companyId: Long, ownerId: Long, scope: Option[Long]): CertificateResponseContract = {
    val c = certificateService.update(id, title, description, validPeriodType, validPeriodValue, isOpenBadgesIntegration, shortDescription, companyId, ownerId, scope)
    toCertificateResponse(c)
  }

  def exportCertificate(companyId: Long, certificateId: Long): InputStream = {
    new CertificateExportProcessor().exportItems(Seq(certificateRepository.getById(certificateId)))
  }

  def exportCertificates(companyId: Long): InputStream = {
    new CertificateExportProcessor().exportItems(certificateRepository.getBy(CertificateFilter(companyId)))
  }

  def importCertificates(file: File, companyId: Long): Unit = {
    new CertificateImportProcessor().importItems(file, companyId)
  }

//  def getAvailableStatements(statementApi: StatementApi, page: Int, skip: Int, take: Int, filter: String, isSortDirectionAsc: Boolean): CollectionResponse[AvailableStatementResponse] = {
//    val resp = certificateService.getAvailableStatements(statementApi ,page, skip, take, filter, isSortDirectionAsc)
//    val inPage = resp.items
//      .map { s =>
//        AvailableStatementResponse(s._1.id, s._1.display, s._2._1, s._2._2)
//      }
//
//    CollectionResponse(page, inPage, resp.total)
//  }

  def getStatesBy(userId: Long, companyId: Long, statuses: Set[CertificateStatuses.Value]) = {
    val certificateStates = certificateStatusChecker.checkAndGetStatus(
      CertificateFilter(companyId, isPublished = Some(true)),
      CertificateStateFilter(Some(userId), statuses = statuses)
    )
    val certificateIds = certificateStates.map(_.certificateId).toSet

    val certificates = certificateRepository.getByIds(certificateIds)
        .map(c => c.id -> c).toMap

    certificateStates
      .map(s => toStateResponse(certificates(s.certificateId), s))
  }
}
