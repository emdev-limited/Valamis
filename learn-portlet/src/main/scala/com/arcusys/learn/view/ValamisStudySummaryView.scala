package com.arcusys.learn.view

import javax.portlet.{RenderRequest, RenderResponse}
import com.arcusys.learn.liferay.permission.{HideStatisticPermission, PermissionUtil}
import com.arcusys.learn.models.response.users.UserResponse
import com.arcusys.learn.view.extensions.{BaseView, OAuthPortlet}
import com.arcusys.valamis.user.service.UserService

class ValamisStudySummaryView extends OAuthPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse): Unit = {
    val userService = inject[UserService]
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)
    val userInfo = new UserResponse(userService.getById(securityScope.userId))

    val data = Map(
        "userName" -> userInfo.name,
        "userPageUrl" -> userInfo.pageUrl,
        "userPicture" -> userInfo.picture,
        "permissionHideStatistic" -> PermissionUtil.hasPermission(securityScope.courseId, securityScope.portletId, securityScope.primaryKey, HideStatisticPermission)
      ) ++ securityScope.data

    sendTextFile("/templates/2.0/valamis_study_summary_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendMustacheFile(data, "valamis_study_summary.html" )
  }

}
