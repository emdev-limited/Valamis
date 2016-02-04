package com.arcusys.valamis.lesson

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.lesson.model.PackageScopeRule
import com.arcusys.valamis.model.ScopeType

import scala.slick.driver.JdbcProfile

trait PackageScopeRuleTableComponent {
  protected val driver: JdbcProfile
  import driver.simple._

  lazy val scopeTypeMapper = MappedColumnType.base[ScopeType.ScopeType, String](
    s => s.toString,
    s => ScopeType.withName(s)
  )

  class PackageScopeRuleTable(tag: Tag) extends Table[PackageScopeRule](tag, tblName("PACKAGE_SCOPE_RULE")) {
    implicit val mapper = scopeTypeMapper
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

