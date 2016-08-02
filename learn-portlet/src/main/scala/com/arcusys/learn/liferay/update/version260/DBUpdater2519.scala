package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.{LNoSuchUserException, LUpgradeProcess}
import com.arcusys.learn.liferay.services.CompanyLocalServiceHelper
import com.arcusys.valamis.certificate.model.{Certificate, CertificateFilter}
import com.arcusys.valamis.certificate.service.CertificateAssetHelper
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

class DBUpdater2519 extends LUpgradeProcess with Injectable {
  val log = LoggerFactory.getLogger(this.getClass)
  implicit val bindingModule = Configuration

  override def getThreshold = 2519

  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val assetHelper = new CertificateAssetHelper()


  override def doUpgrade(): Unit = {
    val companies = CompanyLocalServiceHelper.getCompanies

    val certificateClassName = classOf[Certificate].getName
    for (company <- companies) {

      Try {
        company.getDefaultUser.getUserId
      } match {
        case Failure(e: LNoSuchUserException) => log.warn(e.getMessage, e)
        case Success(defaultUserId) =>
          val filter = CertificateFilter(companyId = company.getCompanyId, isPublished = Some(true))
          val certificates = certificateRepository.getBy(filter)
          certificates.map { c =>
            assetHelper.updateCertificateAssetEntry(c, Some(defaultUserId))
          }
      }
    }
  }
}