package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.{LPermissionChecker, LUser}
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal
import com.liferay.portal.kernel.security.permission.{PermissionCheckerFactoryUtil, PermissionThreadLocal}
import com.liferay.portal.kernel.service.UserLocalServiceUtil

/**
  * Created by asemenov on 22.01.15.
  */
object PermissionHelper {

  def getPermissionChecker(): LPermissionChecker = {
    PermissionThreadLocal.getPermissionChecker
  }

  def getPermissionChecker(user: LUser): LPermissionChecker = {
    PermissionCheckerFactoryUtil.create(user)
  }

  def preparePermissionChecker(userId: Long): Unit = {
    val user = UserLocalServiceUtil.getUserById(userId)
    preparePermissionChecker(user)
  }

  def preparePermissionChecker(user: LUser): Unit = {
    val permissionChecker = PermissionCheckerFactoryUtil.create(user)

    PermissionThreadLocal.setPermissionChecker(permissionChecker)
    PrincipalThreadLocal.setName(user.getUserId)
  }
}
