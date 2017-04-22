package com.arcusys.valamis.web.servlet.course

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.user.util.UserExtension

case class UserResponse(id: Long,
                        name: String,
                        email: String,
                        picture: String = "",
                        pageUrl: String = "",
                        organizations: Set[String] = Set(),
                        roles: Set[String] = Set(),
                        prerequisitesCompleted: Option[Boolean] = None)

object UserResponse {
  def apply(lUser: LUser): UserResponse = UserResponse(
    id = lUser.getUserId,
    name = lUser.getFullName,
    email = lUser.getEmailAddress,
    picture = lUser.getPortraitUrl,
    pageUrl = lUser.getPublicUrl,
    organizations = lUser.getOrganizationNames
  )

  def apply(lUser: LUser, prerequisitesCompleted: Boolean): UserResponse = UserResponse(
    id = lUser.getUserId,
    name = lUser.getFullName,
    email = lUser.getEmailAddress,
    picture = lUser.getPortraitUrl,
    pageUrl = lUser.getPublicUrl,
    organizations = lUser.getOrganizationNames,
    prerequisitesCompleted = Some(prerequisitesCompleted)
  )
}