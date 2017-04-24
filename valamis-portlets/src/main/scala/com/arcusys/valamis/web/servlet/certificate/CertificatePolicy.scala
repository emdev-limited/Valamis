package com.arcusys.valamis.web.servlet.certificate

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{ModifyPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import org.scalatra.ScalatraBase

trait CertificatePolicy {
  self: ScalatraBase =>

  before("/certificates(/)(:id)", request.getMethod == "GET")(
    PermissionUtil.requirePermissionApi(ViewPermission,
      PortletName.CertificateManager,
      PortletName.CertificateViewer,
      PortletName.LearningTranscript,
      PortletName.AchievedCertificates,
      PortletName.LearningPaths)
  )

  before("/certificates/users/:userId", request.getMethod == "GET")(
    if (params.as[Long]("userId") != PermissionUtil.getUserId) {
      PermissionUtil.requirePermissionApi(ViewPermission,
        PortletName.CertificateManager,
        PortletName.LearningTranscript)
    }
  )

  before("/certificates/:id/logo", request.getMethod == "GET")(
    PermissionUtil.requirePermissionApi(ViewPermission,
      PortletName.CertificateManager,
      PortletName.CertificateViewer,
      PortletName.AchievedCertificates,
      PortletName.ValamisActivities,
      PortletName.LearningPaths,
      PortletName.LearningTranscript)
  )

  before(request.getMethod == "PUT")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager)
  )

  before("/certificates(/)(:id)(/)(:resource)(/)", Set("POST", "DELETE").contains(request.getMethod))(
    params.get("resource") match {
      case Some("current-user") =>
        PermissionUtil.requireLogin()
      case _ =>
        PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager)
    }
  )

}