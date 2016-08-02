package com.arcusys.valamis.web.servlet.liferay

import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.servlet.liferay.response.OrgResponse

class OrganizationServlet extends BaseJsonApiController {

  lazy val userService = inject[UserService]

  get("/organizations(/)") {
    userService.getOrganizations
      .map(x => OrgResponse(x.getOrganizationId, x.getName))
  }
}
