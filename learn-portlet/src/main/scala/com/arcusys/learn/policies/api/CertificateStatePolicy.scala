package com.arcusys.learn.policies.api

import com.arcusys.learn.liferay.permission.{PermissionUtil, PortletName, ViewPermission}

trait CertificateStatePolicy extends BasePolicy {
 before("/certificate-states(/)(:userId)") (
   PermissionUtil.requirePermissionApi(ViewPermission,
     PortletName.CompetencesUser,
     PortletName.LearningPaths,
     PortletName.AchievedCertificates)
 )
}
