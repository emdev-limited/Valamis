package com.arcusys.valamis.user.service

import com.arcusys.learn.liferay.LiferayClasses.{LOrganization, LUser}
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.{User, UserFilter}

/**
 * User: Yulia.Glushonkova
 * Date: 14.10.2014
 */
trait UserService {
  def getBy(filter: UserFilter, skipTake: Option[SkipTake] = None): Seq[LUser]

  def getUsersIds(groupId: Long): Seq[Long]

  def getCountBy(filter: UserFilter): Long

  def getById(id: Long): LUser

  def getWithDeleted(id: Long): User

  def getByCourses(courseIds: Seq[Long],
                   companyId: Long,
                   organizationId: Option[Long] = None,
                   nameFilter: Option[String] = None,
                   asc: Boolean = false): Seq[LUser]

  def getByName(name: String, companyId: Long, count: Option[Int] = None): Seq[User]

  def getByIds(companyId: Long, ids: Set[Long]): Seq[LUser]

  def getUsersByGroupOrOrganization(companyId: Long, courseId: Long, organizationId: Option[Long]):Seq[LUser]
}
