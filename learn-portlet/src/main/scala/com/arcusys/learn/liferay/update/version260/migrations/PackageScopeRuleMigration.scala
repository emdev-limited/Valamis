package com.arcusys.learn.liferay.update.version260.migrations

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.learn.liferay.LogFactoryHelper

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}
import com.arcusys.valamis.util.enumeration.EnumerationExtensions
import slick.jdbc.GetResult

class PackageScopeRuleMigration(val db: JdbcBackend#DatabaseDef,
                                val driver: JdbcProfile)
  extends PackageScopeRuleTableComponent with SlickProfile {
  import driver.simple._

  val log = LogFactoryHelper.getLog(getClass)

  case class OldEntity(id: Int,
                       packageId: Option[Int],
                       scope: Option[String],
                       scopeId: Option[String],
                       visibility: Option[Boolean],
                       isDefault: Option[Boolean])

  implicit val getPackageScoreRule = GetResult[OldEntity] { r =>
    OldEntity(
      r.nextInt(),
      r.nextIntOption(),
      r.nextStringOption(),
      r.nextStringOption(),
      r.nextBooleanOption(),
      r.nextBooleanOption()
    )
  }
  def getOldData(implicit session: JdbcBackend#Session) = {
    StaticQuery.queryNA[OldEntity](s"SELECT * FROM learn_lfpackagescoperule").list
      .filter(row => row.packageId.isDefined && row.scope.isDefined)
  }

  def toNewData(entity: OldEntity): Option[PackageScopeRule] = {
    if (entity.scope.exists(ScopeType.isValid))
      Some(PackageScopeRule(
        entity.packageId.get,
        ScopeType.withName(entity.scope.get),
        entity.scopeId,
        entity.visibility.getOrElse(false),
        entity.isDefault.getOrElse(false)
      ))
    else {
      log.warn(s"The value of scope ${entity.scope} is incorrect")
      None
    }
  }

  def migrate(): Unit = {
    db.withTransaction { implicit session =>
      packageScopeRule.ddl.create
      val newRows = getOldData flatMap toNewData
      if (newRows.nonEmpty) packageScopeRule ++= newRows
    }
  }
}
