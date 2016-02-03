package com.arcusys.learn.policies.api

import com.arcusys.learn.liferay.permission._

trait UserPolicy extends BasePolicy {
 before("/users(/)(:userID)", request.getMethod == "GET") (
   if (!PermissionUtil.hasPermissionApi(ModifyPermission, PortletName.CertificateManager, PortletName.CompetencesUser)) {
     PermissionUtil.requirePermissionApi(
       Permission(ViewPermission, Seq(PortletName.LearningTranscript, PortletName.UserPortfolio, PortletName.ValamisActivities)),
       Permission(ViewAllPermission, Seq(PortletName.PhenomenizerReport)))
   }
 )
}
