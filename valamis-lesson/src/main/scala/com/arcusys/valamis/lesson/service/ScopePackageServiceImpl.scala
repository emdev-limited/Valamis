package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.arcusys.learn.liferay.services.GroupLocalServiceHelper
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model.{LessonType, PackageScopeRule}
import com.arcusys.valamis.lesson.scorm.storage.ScormPackagesStorage
import com.arcusys.valamis.lesson.scorm.storage.tracking.AttemptStorage
import com.arcusys.valamis.lesson.storage.{ PackageScopeRuleStorage, PlayerScopeRuleStorage }
import com.arcusys.valamis.lesson.tincan.storage.TincanPackageStorage
import com.arcusys.valamis.model.ScopeType
import com.arcusys.valamis.model.ScopeType.ScopeType
import com.escalatesoft.subcut.inject.{ BindingModule, Injectable }

class ScopePackageServiceImpl(implicit val bindingModule: BindingModule) extends ScopePackageService with Injectable {
  lazy val packageRepository = inject[ScormPackagesStorage]
  lazy val tincanPackageRepository = inject[TincanPackageStorage]
  lazy val packageScopeRuleRepository = inject[PackageScopeRuleStorage]
  lazy val playerScopeRuleRepository = inject[PlayerScopeRuleStorage]
  lazy val attemptStorage = inject[AttemptStorage]

  override def setInstanceScopeSettings(packageId: Long, visibility: Boolean, isDefault: Boolean): PackageScopeRule =
    setVisibilityAndIsDefault(packageId, ScopeType.Instance, None, visibility, isDefault)

  override def setSiteScopeSettings(packageId: Long, siteID: Int, visibility: Boolean, isDefault: Boolean): PackageScopeRule =
    setVisibilityAndIsDefault(packageId, ScopeType.Site, Option(siteID.toString), visibility, isDefault)

  override def setPageScopeSettings(packageId: Long, pageID: String, visibility: Boolean, isDefault: Boolean): PackageScopeRule =
    setVisibilityAndIsDefault(packageId, ScopeType.Page, Option(pageID), visibility, isDefault)

  override def setPlayerScopeSettings(packageId: Long, portletID: String, visibility: Boolean, isDefault: Boolean): PackageScopeRule =
    setVisibilityAndIsDefault(packageId, ScopeType.Player, Option(portletID), visibility, isDefault)

  private def setVisibilityAndIsDefault(packageId: Long,
                                        scope: ScopeType.Value,
                                        scopeId: Option[String],
                                        visibility: Boolean,
                                        isDefault: Boolean): PackageScopeRule = {
    val instanceScope = packageScopeRuleRepository.get(packageId, scope, scopeId)
    instanceScope match {
      case Some(value) =>
        packageScopeRuleRepository.update(
        packageId,
        scope,
        scopeId,
        visibility,
        isDefault)
      case None        =>
        packageScopeRuleRepository.create(
        PackageScopeRule(
          packageId,
          scope,
          scopeId,
          visibility,
          isDefault))
    }
  }

  override def getAllCourseIds(companyId: Long): List[Long] =
    GroupLocalServiceHelper
      .searchExceptPrivateSites(companyId, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .map(i => i.getGroupId)
      .toList

  override def getDefaultPackageID(siteID: String, pageID: String, playerID: String) = {
    val playerScope = playerScopeRuleRepository.get(playerID)
    val scope = if (playerScope.isEmpty) ScopeType.Site else playerScope.get.scope
    val scopeID = scope match {
      case ScopeType.Instance => None
      case ScopeType.Site     => Option(siteID)
      case ScopeType.Page     => Option(pageID)
      case ScopeType.Player   => Option(playerID)
    }
    packageScopeRuleRepository.getDefaultPackageID(scope, scopeID)
  }
}
