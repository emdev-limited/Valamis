package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.{CompanyLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrs.LrsType
import com.arcusys.learn.liferay.update.version260.lrs.ActorsSchema
import com.arcusys.learn.liferay.update.version260.lrs.AccountsSchema
import slick.jdbc.JdbcBackend
import slick.jdbc.meta.MTable
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo}
import com.arcusys.valamis.web.configuration.ioc.Configuration

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBUpdater2507(dbInfo: SlickDBInfo) extends LUpgradeProcess
  with ActorsSchema
  with AccountsSchema {

  override def getThreshold = 2507

  def this() = this(Configuration.inject[SlickDBInfo](None))

  lazy val db = dbInfo.databaseDef

  val driver = dbInfo.slickProfile

  import driver.simple._

  override def doUpgrade(): Unit = {
    val tableActorsName = "lrs_actors"
    val tableAccountsName = "lrs_accounts"

    val hasTables = Await.result(db.run {
      for {
        actorsTable <- MTable.getTables(tableActorsName).headOption
        accountsTable <- MTable.getTables(tableAccountsName).headOption
      } yield actorsTable.isDefined && accountsTable.isDefined
    }, Duration.Inf)

    db.withTransaction { implicit s =>
      if (hasTables) {
        val companies = CompanyLocalServiceHelper.getCompanies
        val actorsWithEmail = actors.filterNot(a => a.mBox === "").list

        actorsWithEmail.foreach { a =>
          updateActor(companies.toList, a._3.get)
        }
      }
    }
  }

  def updateActor(companies: List[LCompany], email: String)(implicit session: JdbcBackend#SessionDef) = {
    val companyForUser = getCompaniesForUser(companies, email)

    val account = companyForUser
      .find { case (company, user) => company.getCompanyId == PortalUtilHelper.getDefaultCompanyId }
      .orElse(companyForUser.headOption)
      .map { case (company, user) => (PortalUtilHelper.getHostName(company.getCompanyId), user.getUuid) }

    for (a <- account) {
      val idAccount = accounts
        .filter { r => r.name === a._2 && r.homePage === a._1 }
        .map { r => r.key }
        .firstOption

      if (idAccount.isDefined) {
        actors.filter(a => a.mBox === email).map(a => (a.accountKey, a.mBox)).update((idAccount, null))
      } else {
        val newAccount = (Some(a._2), Some(a._1))
        val accountId = (accounts returning accounts.map(_.key)) += newAccount

        actors
          .filter(a => a.mBox === email).map(a => (a.accountKey, a.mBox))
          .update((Some(accountId), null))
      }
    }
  }

  def getCompaniesForUser(companies: List[LCompany], email: String): Seq[(LCompany, LUser)] = {
    val mailPrefix = "mailto:"
    companies.flatMap { company =>
      val emailValamis = email.replace(mailPrefix, "")
      Option(UserLocalServiceHelper().fetchUserByEmailAddress(company.getCompanyId, emailValamis))
        .map(user => (company, user))
    }
  }
}





