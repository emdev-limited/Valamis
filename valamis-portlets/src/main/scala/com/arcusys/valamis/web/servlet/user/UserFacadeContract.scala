package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.UserFilter
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import org.joda.time.DateTime

trait UserFacadeContract {

  def getBy(filter: UserFilter,
            page: Option[Int],
            skipTake: Option[SkipTake] = None,
            withStat: Boolean = false): CollectionResponse[UserResponseBase]

  def getById(id: Long): UserResponse

  // def byPermission(permissionType: PermissionType): Seq[UserShortResponse]
  def allCanView(courseId: Long, viewAll: Boolean): Seq[UserResponse]

  def canView(courseId: Long, user: LUser, viewAll: Boolean): Boolean

  def canView(courseId: Long, userId: Long, viewAll: Boolean): Boolean

  def getUserResponseWithCertificateStatus(user: LUser,
                                           userJoinedDate: DateTime,
                                           status: CertificateStatuses.Value): UserWithCertificateStatusResponse
}
