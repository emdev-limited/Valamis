package com.arcusys.valamis.lesson

import com.arcusys.valamis.core.OptionFilterSupport
import com.arcusys.valamis.lesson.model.PackageScopeRule
import com.arcusys.valamis.lesson.storage.PackageScopeRuleStorage
import com.arcusys.valamis.model.ScopeType

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class PackageScopeRuleStorageImpl(val db: JdbcBackend#DatabaseDef,
                                           val driver: JdbcProfile)
  extends PackageScopeRuleStorage with PackageScopeRuleTableComponent with OptionFilterSupport{

  import driver.simple._

  implicit val mapper = scopeTypeMapper

  override def get(packageId: Long, scope: ScopeType.Value, scopeId: Option[String]): Option[PackageScopeRule] =
    db.withSession { implicit s =>
      packageScopeRule.filter(x => x.packageId === packageId && x.scope === scope && optionFilter(x.scopeId, scopeId)).firstOption
    }

  override def getAll(packageId: Long, scope: ScopeType.Value, scopeId: Option[String]): Seq[PackageScopeRule] =
    db.withSession { implicit s =>
      packageScopeRule.filter(x => x.packageId === packageId && x.scope === scope && optionFilter(x.scopeId, scopeId)).list
    }

  override def update(packageId: Long, scope: ScopeType.Value, scopeId: Option[String], visibility: Boolean, isDefault: Boolean) = {
    if (isDefault) cleanIsDefault(scope, scopeId)
    db.withSession { implicit s =>
      val filtered = packageScopeRule.filter(x => x.packageId === packageId && x.scope === scope && optionFilter(x.scopeId, scopeId))
      filtered.map(rule => (rule.isDefault, rule.visibility)).update(isDefault, visibility)
      filtered.first
    }
  }

  override def delete(packageId: Long) =
    db.withSession { implicit s =>
      packageScopeRule.filter(_.packageId === packageId).delete
    }

  override def getDefaultPackageID(scope: ScopeType.Value, scopeId: Option[String]): Option[Long] =
    db.withSession { implicit s =>
      packageScopeRule.filter(x => x.scope === scope && optionFilter(x.scopeId, scopeId) && x.isDefault === true).map(_.packageId).firstOption
    }

  override def create(entity: PackageScopeRule): PackageScopeRule =
    db.withSession { implicit s =>
      val newId = (packageScopeRule returning packageScopeRule.map(_.id)) += entity
      entity.copy(id = Option(newId))
    }

  override def getPackageIdVisible(scope: ScopeType.Value, scopeId: Option[String]): List[Long] =
    db.withSession { implicit s =>
      packageScopeRule.filter(x => x.scope === scope && optionFilter(x.scopeId, scopeId) && x.visibility === true).map(_.packageId).list
    }

  override def updatePackageIndex(packageId: Long, scope: ScopeType.Value, index: Long): Unit =
    db.withSession { implicit s =>
      packageScopeRule
        .filter(x => x.packageId === packageId && x.scope === scope)
        .map(_.index)
        .update(Some(index))
    }

  override def getByScope(scope: ScopeType.Value): Map[Long, Long] =
    db.withSession { implicit s =>
      packageScopeRule
        .filter(x => x.index.isDefined && x.scope === scope)
        .map(x => (x.packageId, x.index.get))
        .list
        .toMap
    }

  private def cleanIsDefault(scope: ScopeType.Value, scopeId: Option[String]) {
    db.withSession { implicit s =>
      packageScopeRule.filter(x => x.scope === scope && optionFilter(x.scopeId, scopeId))
        .map(_.isDefault).update(false)
    }
  }
}
