package com.arcusys.valamis.user.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.arcusys.learn.liferay.services.dynamicQuery._
import com.arcusys.learn.liferay.services.{OrganizationLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.user.model.{User, UserFilter, UserSort}
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.kernel.workflow.WorkflowConstants

import scala.collection.JavaConversions._
// TODO: refactor (get by id)
abstract class UserServiceImpl extends UserService {

  def userCertificateRepository: UserCertificateRepository
  private lazy val userLocalService = UserLocalServiceHelper()

  def removedUserPrefix: String

  def getUsersIds(groupId: Long): Seq[Long] = {
    val groupUsers = userLocalService.getGroupUserIds(groupId)

    val organizationsUsers = OrganizationLocalServiceHelper.getGroupOrganizationsIds(groupId)
      .flatMap(userLocalService.getOrganizationUserIds)

    (groupUsers ++ organizationsUsers).distinct
  }

  def getBy(filter: UserFilter, skipTake: Option[SkipTake]): Seq[LUser] = {
    try {
      val query = getQuery(filter)

      filter.sortBy match {
        case Some(UserSort(_, Order.Asc)) =>
          query.addOrder(OrderFactoryUtil.asc("firstName"))
            .addOrder(OrderFactoryUtil.asc("lastName"))
        case _ =>
          query.addOrder(OrderFactoryUtil.desc("firstName"))
            .addOrder(OrderFactoryUtil.desc("lastName"))
      }

      val result = skipTake match {
        case Some(SkipTake(skip, take)) if take > 0 =>
          userLocalService.dynamicQuery(query, skip, skip + take)
        case _ =>
          userLocalService.dynamicQuery(query)
      }

      result.map(_.asInstanceOf[LUser])
    } catch {
      case e: UselessRestrictionException => Nil
    }

}

  def getCountBy(filter: UserFilter): Long = {
    try {
      val query = getQuery(filter).setProjection(ProjectionFactoryUtil.rowCount())
      userLocalService.dynamicQueryCount(query)
    } catch {
      case e: UselessRestrictionException => 0
    }
  }

  private def getQuery(filter: UserFilter): DynamicQuery = {
    val query = userLocalService.dynamicQuery
      .add(RestrictionsFactoryUtil.eq("status", WorkflowConstants.STATUS_APPROVED)) // isActive
      .add(RestrictionsFactoryUtil.eq("defaultUser", false))
    
    for(companyId <- filter.companyId)
      query.add(RestrictionsFactoryUtil.eq("companyId", companyId))
    
    for (value <- filter.namePart) {
      query.add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.ilike("firstName", "%" + value + "%"),
        RestrictionsFactoryUtil.ilike("lastName", "%" + value + "%")
      ))
    }

    for (certificateId <- filter.certificateId) {
      val userIds = userCertificateRepository.getUsersBy(certificateId)
      query.addFilterByValues("userId", userIds, contains = filter.isUserJoined)
    }

    val orgAndGroupUserIds = filter.organizationId.fold(Seq.empty[Long]){ orgId =>
      userLocalService.getOrganizationUserIds(orgId)
    } ++ filter.groupId.fold(Seq.empty[Long]){ groupId =>
      val organizations = OrganizationLocalServiceHelper
        .getGroupOrganizationsIds(groupId)

      userLocalService.getGroupUserIds(groupId) ++
        organizations.flatMap(userLocalService.getOrganizationUserIds)
    }

    if(filter.withUserIdFilter && filter.userIds.nonEmpty) {
      query.addFilterByValues("userId", filter.userIds, contains = filter.isUserJoined)
    }

    if(orgAndGroupUserIds.nonEmpty) {
      query.addFilterByValues("userId", orgAndGroupUserIds, contains = filter.isUserJoined)
    }

    query
  }

  def getById(id: Long): LUser = userLocalService.getUser(id)

  def getWithDeleted(id: Long): User = {
    userLocalService.fetchUser(id)
      .map(new User(_))
      .getOrElse(User(id, s"$removedUserPrefix $id", isDeleted = true))
  }

  def getByCourses(courseIds: Seq[Long],
                   companyId: Long,
                   organizationId: Option[Long],
                   nameFilter: Option[String],
                   asc: Boolean): Seq[LUser] = {
    val filter = UserFilter(
      companyId = Some(companyId),
      organizationId = organizationId,
      namePart = nameFilter
    )
    val query = getQuery(filter)
    val userIds = courseIds.flatMap(userLocalService.getGroupUserIds).distinct
    query.addFilterByValues("userId", userIds, contains = true)
    userLocalService.dynamicQuery(query, asc, None)
  }

  def getByName(name: String, companyId: Long, count: Option[Int] = None): Seq[User] = {
    userLocalService.searchByName(companyId, name, None, count)
      .map(new User(_))
  }

  def getByIds(companyId: Long, ids: Set[Long]): Seq[LUser] = {
    //todo: remove filtering in memory
    userLocalService
      .getCompanyUsers(companyId, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .filter(user => ids.contains(user.getUserId))
  }

  private def getAll(companyId: Long): Seq[LUser] = {
    userLocalService.getCompanyUsers(companyId)
  }

  def getUsersByGroupOrOrganization(companyId: Long,
                                    courseId: Long,
                                    organizationId: Option[Long]): Seq[LUser] = {
    val courseUsersIds = getUsersIds(courseId)

    val usersIds = organizationId match {
      case None => courseUsersIds
      case Some(orgId) => courseUsersIds intersect userLocalService.getOrganizationUserIds(orgId)
    }

    usersIds map userLocalService.getUser
  }
}
