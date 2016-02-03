package com.arcusys.valamis.user.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.{ScormUser, UserFilter, User}
import com.liferay.portal.model.Organization

/**
 * User: Yulia.Glushonkova
 * Date: 14.10.2014
 */
trait UserService {
  def all(companyId: Long): Seq[LUser]

  def all(courseId: Long,
    filter: String,
    sortAZ: Boolean): Seq[LUser]

  def getBy(filter: UserFilter, skipTake: Option[SkipTake] = None): Seq[LUser]

  def getCountBy(filter: UserFilter): Long

  def getById(id: Long): LUser


  def getByName(name: String, companyId: Long, count: Option[Int] = None): Seq[User]

  def getByIds(companyId: Long, ids: Set[Long]): Seq[LUser]

  def getOrganizations: Seq[Organization]

  def getUserOption(userId: Int): Option[ScormUser]

  def createAndGetId(userId: Int, name: String)
}
