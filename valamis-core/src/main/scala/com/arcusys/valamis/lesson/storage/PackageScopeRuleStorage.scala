package com.arcusys.valamis.lesson.storage

import com.arcusys.valamis.lesson.model.PackageScopeRule
import com.arcusys.valamis.model.ScopeType

trait PackageScopeRuleStorage {
  def get(packageId: Long, scope: ScopeType.Value, scopeId: Option[String]): Option[PackageScopeRule]
  def getAll(packageId: Long, scope: ScopeType.Value, scopeId: Option[String]): Seq[PackageScopeRule]
  def getPackageIdVisible(scope: ScopeType.Value, scopeID: Option[String]): List[Long]
  def create(entity: PackageScopeRule) : PackageScopeRule
  def update(packageId: Long, scope: ScopeType.Value, scopeID: Option[String], visibility: Boolean, isDefault: Boolean): PackageScopeRule
  def updatePackageIndex(packageId: Long, scope: ScopeType.Value, index: Long) : Unit
  def getByScope(scope: ScopeType.Value): Map[Long, Long]
  def getDefaultPackageID(scope: ScopeType.Value, scopeId: Option[String]): Option[Long]
  def delete(packageId: Long)
}
