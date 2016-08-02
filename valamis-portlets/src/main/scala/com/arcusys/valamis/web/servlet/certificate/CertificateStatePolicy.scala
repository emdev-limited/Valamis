package com.arcusys.valamis.web.servlet.certificate

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import org.scalatra.ScalatraBase

trait CertificateStatePolicy {
  self: ScalatraBase =>

  before("/certificate-states(/)(:userId)")(
    PermissionUtil.requirePermissionApi(ViewPermission,
      PortletName.LearningPaths,
      PortletName.AchievedCertificates)
  )
}
