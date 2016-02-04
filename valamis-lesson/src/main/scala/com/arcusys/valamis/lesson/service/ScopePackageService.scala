package com.arcusys.valamis.lesson.service

import com.arcusys.valamis.lesson.model.PackageScopeRule

trait ScopePackageService {

  def setInstanceScopeSettings(packageId: Long, visibility: Boolean, isDefault: Boolean): PackageScopeRule
  def setSiteScopeSettings(packageId: Long, siteID: Int, visibility: Boolean, isDefault: Boolean): PackageScopeRule
  def setPageScopeSettings(packageId: Long, pageID: String, visibility: Boolean, isDefault: Boolean): PackageScopeRule
  def setPlayerScopeSettings(packageId: Long, portletID: String, visibility: Boolean, isDefault: Boolean): PackageScopeRule
  def getAllCourseIds(companyId: Long): List[Long]
  def getDefaultPackageID(siteID: String, pageID: String, playerID: String): Option[Long]
}
