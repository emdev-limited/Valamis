package com.arcusys.valamis.user.model

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.UserGroupRoleLocalServiceHelper
import com.arcusys.valamis.user.util.UserExtension

case class UserInfo(id: Long,
                    name: String,
                    email: String,
                    picture: String = "",
                    pageUrl: String = "",
                    organizations: Set[String] = Set(),
                    roles: Set[String] = Set()) {

  def this(lUser: LUser) = this(
    id = lUser.getUserId,
    name = lUser.getFullName,
    email = lUser.getEmailAddress,
    picture = lUser.getPortraitUrl,
    pageUrl = lUser.getPublicUrl,
    organizations = lUser.getOrganizationNames
  )

  def this(lUser: LUser, groupId: Long) = this(
    id = lUser.getUserId,
    name = lUser.getFullName,
    email = lUser.getEmailAddress,
    picture = lUser.getPortraitUrl,
    pageUrl = lUser.getPublicUrl,
    organizations = lUser.getOrganizationNames,
    roles = UserGroupRoleLocalServiceHelper.getSiteRolesForUser(lUser.getUserId, groupId).map(_.getName).toSet
  )
}