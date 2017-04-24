package com.arcusys.valamis.updaters.version320

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.{CompanyLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version320.schema3203._
import slick.jdbc.meta.MTable

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class DBUpdater3203 extends BaseDBUpdater
  with ActorsSchema
  with AccountsSchema
  with AgentProfileSchema
  with ContextSchema
  with StatementObjectSchema
  with StatementSchema
  with StateProfileSchema
  with SubStatementSchema
  with DatabaseLayer {

  override def getThreshold = 3203

  import driver.api._

  def clearDuplicatedActors: Unit = execSyncInTransaction {
    val fetchDupAction = actors.filter(_.accountKey.isDefined).groupBy(_.accountKey).map {
      case (accountKey, list) => (accountKey, list.length)
    }.filter(_._2 > 1).map(_._1).result


    for {
      accountList <- fetchDupAction
      _ <- {
        DBIO.sequence(
          accountList.map { accountId =>
            val actorIds = actors.filter(_.accountKey === accountId).drop(1).map(_.key)
            val actorOptionalIds = actors.filter(_.accountKey === accountId).drop(1).map(_.key.?)
            for {
              actorId <- actors.filter(_.accountKey === accountId).map(_.key).result.head
              actorIdValues <- actorIds.result
              _ <- statements.filter(_.actorKey in actorIds).map(_.actorKey).update(actorId)
              _ <- statements.filter(_.objectKey in actorIds).map(_.objectKey).update(actorId)
              _ <- agentProfiles.filter(_.agentKey in actorIds).map(_.agentKey).update(actorId)
              _ <- stateProfiles.filter(_.agentKey in actorIds).map(_.agentKey).update(actorId)
              _ <- subStatements.filter(_.actorKey in actorIds).map(_.actorKey).update(actorId)
              _ <- subStatements.filter(_.statementObjectKey in actorIds).map(_.statementObjectKey).update(actorId)
              _ <- contexts.filter(_.instructor in actorOptionalIds).map(_.instructor).update(Some(actorId))
              _ <- contexts.filter(_.team in actorOptionalIds).map(_.team).update(Some(actorId))
              _ <- actors.filter(_.key in actorIds).delete
              _ <- statementObjects.filter(_.key inSet actorIdValues).delete
            } yield ()
          }
        )
      }
    } yield ()
  }

  override def doUpgrade(): Unit = {
    val tableActorsName = "lrs_actors"
    val tableAccountsName = "lrs_accounts"

    val hasTables = execSync(
      for {
        actorsTable <- MTable.getTables(tableActorsName).headOption
        accountsTable <- MTable.getTables(tableAccountsName).headOption
      } yield actorsTable.isDefined && accountsTable.isDefined
    )

    execSyncInTransaction {
      if (hasTables) {
        val companies = CompanyLocalServiceHelper.getCompanies
        val actorsWithEmail = actors.filterNot(a => a.mBox.isEmpty || a.mBox === "").result

        actorsWithEmail.flatMap { res =>
          DBIO.sequence(
            res.map { a =>
              updateActor(companies, a._4.get)
            }
          )
        }
      }
      else {
        DBIO.successful()
      }
    }

    clearDuplicatedActors
  }

  def updateActor(companies: List[LCompany], email: String) = {
    val companyForUser = getCompaniesForUser(companies, email)

    val account = companyForUser
      .find { case (company, user) => company.getCompanyId == PortalUtilHelper.getDefaultCompanyId }
      .orElse(companyForUser.headOption)
      .map { case (company, user) => (PortalUtilHelper.getHostName(company.getCompanyId), user.getUuid) }

    val res = account map { a =>
      val accountAction = accounts
        .filter { r => r.name === a._2 && r.homePage === a._1 }
        .map { r => r.key }
        .result.headOption

      for {
        accountId <- accountAction
        _ <- if (accountId.isDefined) {
          actors.filter(a => a.mBox === email).map(a => (a.accountKey, a.mBox)).update((accountId, null))
        } else {
          val newAccount = (0L, Some(a._2), Some(a._1))
          val newAccountAction = (accounts returning accounts.map(_.key)) += newAccount
          newAccountAction.flatMap { newAccountKey =>
            actors
              .filter(a => a.mBox === email).map(a => (a.accountKey, a.mBox))
              .update((Some(newAccountKey), null))
          }
        }
      } yield ()
    }
    res.getOrElse(DBIO.successful())
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





