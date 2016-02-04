package com.arcusys.learn.liferay.update.version260

import java.sql.SQLException

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.persistence.liferay.model.LFUser
import com.arcusys.slick.drivers.SQLServerDriver
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.valamis.lrs.LrsType
import com.arcusys.valamis.lrs.tincan.Account
import com.liferay.portal.model.Company
import com.liferay.portal.service.{UserLocalServiceUtil, CompanyLocalServiceUtil}
import com.liferay.portal.util.PortalUtil

import scala.collection.JavaConverters._
import com.arcusys.learn.liferay.update.version260.lrs.ActorsSchema
import com.arcusys.learn.liferay.update.version260.lrs.AccountsSchema
import com.arcusys.valamis.core.{LongKeyTableComponent, SlickProfile}
import com.arcusys.valamis.lrs.util.TincanHelper

import scala.slick.jdbc.{StaticQuery, JdbcBackend}
import scala.slick.jdbc.meta.MTable

class DBUpdater2507(dbInfo: SlickDBInfo) extends LUpgradeProcess
with ActorsSchema
with AccountsSchema {

  override def getThreshold = 2507

  def this() = this(Configuration.inject[SlickDBInfo](None))

  lazy val db = dbInfo.databaseDef

  val driver = dbInfo.slickProfile

  import driver.simple._

  override def doUpgrade(): Unit = {
    val lrsType = LrsType.Simple

    db.withTransaction { implicit s =>
      val tableActorsName = "lrs_actors"
      val tableAccountsName = "lrs_accounts"
      val hasTables =  MTable.getTables(tableActorsName).firstOption.isDefined && MTable.getTables(tableAccountsName).firstOption.isDefined


      if (hasTables){
        val companies = CompanyLocalServiceUtil.getCompanies(-1, -1)
        val actorsWithEmail = actors.filterNot(a => a.mBox === "").list

        actorsWithEmail.foreach { a =>
          updateActor(companies.asScala.toList, a._3.get)
        }
      }
    }
  }

  def updateActor(companies: List[Company], email: String)(implicit session: JdbcBackend#SessionDef) = {
    val companyForUser = getCompaniesForUser(companies, email)

    val account = companyForUser
      .find { case (company, user) => company.getCompanyId == PortalUtil.getDefaultCompanyId }
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

  def getCompaniesForUser(companies: List[Company], email: String): Seq[(Company, LUser)] = {
    val mailPrefix = "mailto:"
    companies.flatMap { company =>
      val emailValamis = email.replace(mailPrefix, "")
      Option(UserLocalServiceUtil.fetchUserByEmailAddress(company.getCompanyId, emailValamis))
        .map(user => (company, user))
    }
  }
}





