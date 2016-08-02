package com.arcusys.learn.liferay.services

import com.liferay.portal.model.{RoleConstants, Role, UserGroupRole}
import com.liferay.portal.service.{RoleLocalServiceUtil, UserGroupRoleLocalServiceUtil}

import scala.collection.JavaConverters._

object UserGroupRoleLocalServiceHelper {
  def addUserGroupRoles(userId: Long, groupId: Long, roleIds: Array[Long]): java.util.List[UserGroupRole] =
    UserGroupRoleLocalServiceUtil.addUserGroupRoles(userId, groupId, roleIds)

  def setUserGroupRoles(userId: Long, groupId: Long, roleIds: Array[Long]): java.util.List[UserGroupRole] = {
    UserGroupRoleLocalServiceUtil.deleteUserGroupRoles(userId, Seq(groupId).toArray)
    UserGroupRoleLocalServiceUtil.addUserGroupRoles(userId, groupId, roleIds)
  }

  def setUserGroupRoles(userIds: Seq[Long], groupId: Long, roleIds: Array[Long]): Unit = {
    userIds.foreach(setUserGroupRoles(_,groupId,roleIds))
  }

  def getPossibleSiteRoles(companyId: Long): Seq[Role] = {
    RoleLocalServiceUtil.getRoles(companyId).asScala
      .filter(isSiteRole)
      .filter(notSiteMember)
  }

  def getSiteRolesForUser(userId: Long, groupId: Long): Seq[Role] = {
    UserGroupRoleLocalServiceUtil.getUserGroupRoles(userId,groupId).asScala.map(_.getRole).filter(notSiteMember)
  }

  def notSiteMember(role: Role): Boolean = role.getName != RoleConstants.SITE_MEMBER
  def isSiteRole(role: Role): Boolean = role.getType == RoleConstants.TYPE_SITE
}
