package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.base.{HideStatisticPermission, OAuthPortlet, PermissionUtil, PortletBase}

class ValamisStudySummaryView extends OAuthPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse): Unit = {
    val userService = inject[UserService]
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)
    val userInfo = new UserInfo(userService.getById(securityScope.userId))
    val permission = new PermissionUtil(request, this)


    val data = Map(
        "userName" -> userInfo.name,
        "userPageUrl" -> userInfo.pageUrl,
        "userPicture" -> userInfo.picture,
        "permissionHideStatistic" -> permission.hasPermission(HideStatisticPermission.name)
      ) ++ securityScope.data

    sendTextFile("/templates/2.0/valamis_study_summary_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendMustacheFile(data, "valamis_study_summary.html" )
  }

}
