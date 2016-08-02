package com.arcusys.valamis.web.portlet.base

import javax.portlet.{GenericPortlet, RenderRequest}

import com.arcusys.learn.liferay.LiferayClasses.{LCompany, LLiferayPortletSession}
import com.arcusys.learn.liferay.util.PortalUtilHelper

trait TemplateCoupler { self: GenericPortlet =>

  /**
   * data ( <br/>
   * "contextPath" -> contextPath, <br/>
   * "isPortlet" -> true, <br/>
   * "portletId" -> portletId, <br/>
   * "primaryKey" -> primaryKey, <br/>
   * "userID" -> userId, <br/>
   * "companyID" -> companyId, <br/>
   * "permissionToModify" -> permissionToModify) <br/>
   * @param request RenderRequest
   * @return SecurityData
   */
  def getSecurityData(request: RenderRequest): SecurityData = {

    val contextPath = PortalUtilHelper.getPathContext(request)
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    val portletId = PortalUtilHelper.getPortletId(request)
    val courseId = themeDisplay.getScopeGroupId
    val primaryKey = themeDisplay.getLayout.getPlid + LLiferayPortletSession.LayoutSeparator + portletId

    val permission = new PermissionUtil(request, this)

    val permissionToView = permission.hasPermission(PermissionKeys.View)
    val permissionToModify = permission.hasPermission(PermissionKeys.Modify)

    val userId = themeDisplay.getUserId
    val companyId = PortalUtilHelper.getCompanyId(request)
    val company = themeDisplay.getCompany

    new SecurityData(
      contextPath,
      portletId,
      primaryKey,
      userId,
      companyId,
      courseId,
      permissionToModify,
      permissionToView,
      company
    )
  }

  case class SecurityData(
      contextPath: String,
      portletId: String,
      primaryKey: String,
      userId: Long,
      companyId: Long,
      courseId: Long,
      permissionToModify: Boolean,
      permissionToView: Boolean,
      company: LCompany) {

    var data: Map[String, Any] = Map(
      "contextPath" -> contextPath,
      "isPortlet" -> true,
      "portletId" -> portletId,
      "primaryKey" -> primaryKey,
      "userID" -> userId,
      "companyID" -> companyId,
      "permissionToModify" -> permissionToModify
    )

  }

}
