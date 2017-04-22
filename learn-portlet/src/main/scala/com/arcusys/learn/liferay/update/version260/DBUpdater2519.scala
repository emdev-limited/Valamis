package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.{LNoSuchUserException, LUpgradeProcess}
import com.arcusys.learn.liferay.services.CompanyLocalServiceHelper
import com.arcusys.learn.liferay.update.version240.certificate.CertificateTableComponent
import com.arcusys.valamis.liferay.AssetHelper
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import slick.driver._
import slick.jdbc._

import scala.util.{Failure, Success, Try}

case class Certificate(id: Long,
                       title: String,
                       description: String,
                       logo: String = "",
                       isPermanent: Boolean = true,
                       isPublishBadge: Boolean = false,
                       shortDescription: String = "",
                       companyId: Long,
                       validPeriodType: PeriodTypes.Value = PeriodTypes.UNLIMITED,
                       validPeriod: Int = 0,
                       createdAt: DateTime,
                       isActive: Boolean = false,
                       scope: Option[Long] = None)

class DBUpdater2519(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends LUpgradeProcess
    with Injectable
    with CertificateTableComponent
    with SlickProfile
    with DatabaseLayer {

  val log = LoggerFactory.getLogger(this.getClass)
  implicit val bindingModule = Configuration

  def this(dbInfo: SlickDBInfo) = this(dbInfo.databaseDef, dbInfo.slickDriver)

  def this() = this(Configuration.inject[SlickDBInfo](None))

  override def getThreshold = 2519

  import driver.api._

  private lazy val assetHelper = new AssetHelper[Certificate]

  override def doUpgrade(): Unit = {
    val companies = CompanyLocalServiceHelper.getCompanies

    for (company <- companies) {
      Try {
        company.getDefaultUser.getUserId
      } match {
        case Failure(e: LNoSuchUserException) => log.warn(e.getMessage, e)
        case Success(defaultUserId) =>
          execSync {
            certificates
              .filter(c => c.companyId === company.getCompanyId && c.isPublished)
              .result
          } map { c =>
            val certificate = Certificate.tupled(c)
            assetHelper.updateAssetEntry(
              certificate.id,
              Some(defaultUserId),
              None,
              Some(certificate.title),
              Some(certificate.description),
              c,
              Some(certificate.companyId),
              isVisible = true
            )
          }
      }
    }
  }
}