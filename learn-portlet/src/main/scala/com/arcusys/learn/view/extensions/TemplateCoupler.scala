package com.arcusys.learn.view.extensions

import java.io.FileInputStream
import javax.portlet.{GenericPortlet, PortletContext, RenderRequest}
import javax.servlet.ServletContext

import com.arcusys.learn.liferay.permission.{ModifyPermission, PermissionUtil, ViewPermission}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.view.liferay.LiferayHelpers
import com.liferay.portal.kernel.portlet.LiferayPortletSession
import com.liferay.portal.model.Company
import com.liferay.portal.util.PortalUtil
import org.scalatra.{ScalatraFilter, ScalatraServlet}

trait TemplateCoupler {

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
    val portletId = PortalUtil.getPortletId(request)
    val courseId = themeDisplay.getScopeGroupId
    val primaryKey = themeDisplay.getLayout.getPlid + LiferayPortletSession.LAYOUT_SEPARATOR + portletId

    val permissionToView = PermissionUtil.hasPermission(courseId, portletId, primaryKey, ViewPermission)
    val permissionToModify = PermissionUtil.hasPermission(courseId, portletId, primaryKey, ModifyPermission)

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
      company: Company) {

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
