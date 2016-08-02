package com.arcusys.valamis.web.servlet

import com.arcusys.learn.liferay.services.ClassNameLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.settings.storage.ActivityToStatementStorage
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.social.request.ActivityToStatementRequest

class ActivityToStatementServlet extends BaseApiController {

  lazy val activityToStatementStorage = inject[ActivityToStatementStorage]

  before(request.getMethod == "POST") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ActivityToLRSMapper)
  }

  post("/activityToStatement(/)") {
    val req = ActivityToStatementRequest(this)
    val classNameId = ClassNameLocalServiceHelper.getClassNameId(req.activityClassName)
    activityToStatementStorage.set(req.courseId, classNameId, req.verb)
  }
}
