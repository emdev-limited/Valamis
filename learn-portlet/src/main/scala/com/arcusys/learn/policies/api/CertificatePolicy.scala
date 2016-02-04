package com.arcusys.learn.policies.api

import com.arcusys.learn.liferay.permission.{ModifyPermission, PermissionUtil, PortletName, ViewPermission}

trait CertificatePolicy extends BasePolicy {

  before("/certificates(/)(:id)", request.getMethod == "GET") (
        PermissionUtil.requirePermissionApi(ViewPermission,
        PortletName.CertificateManager,
        PortletName.CertificateViewer,
        PortletName.LearningTranscript,
        PortletName.AchievedCertificates,
        PortletName.LearningPaths)
  )

  before("/certificates/:id/logo", request.getMethod == "GET") (
    PermissionUtil.requirePermissionApi(ViewPermission,
      PortletName.CertificateManager, PortletName.CertificateViewer,
      PortletName.AchievedCertificates, PortletName.ValamisActivities, PortletName.LearningPaths,
      PortletName.LearningTranscript, PortletName.UserPortfolio)
  )

  before("/certificates/:id/do/:action(/)", request.getMethod == "GET") (
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager, PortletName.CertificateViewer)
  )

  before("/certificates/:id/users(/)") (
      PermissionUtil.requirePermissionApi(ViewPermission, PortletName.CertificateManager, PortletName.CertificateViewer)
  )

  before("/certificates(/)(:id)(/)(:resource)(/)", Set("POST", "PUT", "DELETE").contains(request.getMethod)) (
    Symbol(params.getOrElse("resource", "")) match {
      case 'user =>
        PermissionUtil.requireCurrentLoggedInUser(params.getOrElse("userID", "0").toLong)
      case _ =>
        PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager, PortletName.CertificateViewer)
    }
  )

}