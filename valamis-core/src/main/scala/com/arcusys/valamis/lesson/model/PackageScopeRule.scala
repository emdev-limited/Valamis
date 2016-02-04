package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.model.ScopeType

/**
  * A rule for package in scope
  * @param packageId  ID of scorm package
  * @param scope      Type of scope
  * @param scopeId    Scope Layout ID (it might be SiteID, pageID or portletID)
  * @param visibility Identifies if package is visible in scope
  * @param isDefault  Identifies if package is default in current scope
  */
case class PackageScopeRule(packageId: Long,
                            scope: ScopeType.Value,
                            scopeId: Option[String],
                            visibility: Boolean = false,
                            isDefault: Boolean = false,
                            index: Option[Long]= None,
                            id: Option[Long] = None)