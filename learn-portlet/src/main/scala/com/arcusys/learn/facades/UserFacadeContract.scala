package com.arcusys.learn.facades

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.models.OrgResponse
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.learn.models.response.users.{UserResponse, UserResponseBase}
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.UserFilter

trait UserFacadeContract {

  def getBy(filter: UserFilter,
            page: Option[Int],
            skipTake: Option[SkipTake] = None,
            withStat: Boolean = false
             ): CollectionResponse[UserResponseBase]

  def getById(id: Long): UserResponse

  // def byPermission(permissionType: PermissionType): Seq[UserShortResponse]
  def allCanView(courseId: Long, viewAll: Boolean): Seq[UserResponse]

  def canView(courseId: Long, user: LUser, viewAll: Boolean): Boolean

  def canView(courseId: Long, userId: Long, viewAll: Boolean): Boolean

  def getOrganizations: Seq[OrgResponse]
}
