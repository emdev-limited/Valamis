package com.arcusys.learn.liferay.update.version260.migrations

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}

object ScopeType extends Enumeration {
  type ScopeType = Value
  val Instance = Value("instance")
  val Site = Value("site")
  val Page = Value("page")
  val Player = Value("player")
}

case class PackageScopeRule(packageId: Long,
                            scope: ScopeType.Value,
                            scopeId: Option[String],
                            visibility: Boolean = false,
                            isDefault: Boolean = false,
                            index: Option[Long]= None,
                            id: Option[Long] = None)

trait PackageScopeRuleTableComponent extends TypeMapper { self: SlickProfile =>

  import driver.simple._

  implicit val scopeTypeMapper = enumerationMapper(ScopeType)

  class PackageScopeRuleTable(tag: Tag) extends Table[PackageScopeRule](tag, tblName("PACKAGE_SCOPE_RULE")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def packageId = column[Long]("PACKAGE_ID")
    def scope = column[ScopeType.ScopeType]("SCOPE", O.Length(3000, varying = true))
    def scopeId = column[Option[String]]("SCOPE_ID", O.Length(3000, varying = true))
    def visibility = column[Boolean]("VISIBILITY")
    def isDefault = column[Boolean]("IS_DEFAULT")
    def index = column[Option[Long]]("INDEX")

    def * = (packageId, scope, scopeId, visibility, isDefault, index, id.?) <>(PackageScopeRule.tupled, PackageScopeRule.unapply)
  }
  val packageScopeRule = TableQuery[PackageScopeRuleTable]
}

