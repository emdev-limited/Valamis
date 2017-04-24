package com.arcusys.valamis.web.servlet.liferay

import com.arcusys.learn.liferay.services.OrganizationLocalServiceHelper
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.liferay.response.OrgResponse

class OrganizationServlet extends BaseJsonApiController {

  get("/organizations(/)") {
    OrganizationLocalServiceHelper.getOrganizations(PermissionUtil.getCompanyId)
      .map(x => OrgResponse(x.getOrganizationId, x.getName))
  }
}
