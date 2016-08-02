package com.arcusys.valamis.user.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.arcusys.learn.liferay.services.{GroupLocalServiceHelper, OrganizationLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.user.model._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.kernel.workflow.WorkflowConstants

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
// TODO: refactor (get by id)
class UserServiceImpl(implicit val bindingModule: BindingModule)
  extends UserService
  with Injectable {

  private lazy val userCertificateRepository = inject[UserCertificateRepository]
  private lazy val userLocalService = UserLocalServiceHelper()

  def all(courseId: Long, nameFilter: Option[String], sortAZ: Boolean): Seq[LUser] =
    getBy(UserFilter(
      namePart = nameFilter,
      groupId = Some(courseId),
      sortBy = Some(UserSort(UserSortBy.Name, Order.apply(sortAZ)))
    ))

  def getBy(filter: UserFilter, skipTake: Option[SkipTake]) = {
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

    result.asScala.map(_.asInstanceOf[LUser])

  }

  def getCountBy(filter: UserFilter) = {
    val query = getQuery(filter).setProjection(ProjectionFactoryUtil.rowCount())
    userLocalService.dynamicQueryCount(query)
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
      addFilterByIds(query, userCertificateRepository.getUsersBy(certificateId), filter.isUserJoined)
    }

    for (organizationId <- filter.organizationId) {
      addFilterByIds(query, userLocalService.getOrganizationUserIds(organizationId))
    }

    for(groupId <- filter.groupId) {
      val organizations = OrganizationLocalServiceHelper.getGroupOrganizations(groupId).map(_.getOrganizationId)
      val userIds = userLocalService.getGroupUserIds(groupId) ++
        organizations.flatMap(userLocalService.getOrganizationUserIds)
      addFilterByIds(query, userIds.distinct)
    }
    query
  }

  private def addFilterByIds(query: DynamicQuery, userIds: Seq[Long], contains: Boolean = true): Unit = {
    (contains, userIds) match {
      case (true, Seq()) => query.add(RestrictionsFactoryUtil.eq("userId", null)) // abort query
      case (false, Seq()) =>
      case (true, ids:Seq[Long]) => query.add(RestrictionsFactoryUtil.in("userId", userIds))
      case (false, ids:Seq[Long]) => query.add(RestrictionsFactoryUtil.not(RestrictionsFactoryUtil.in("userId", userIds)))
    }
  }

  def getById(id: Long): LUser = userLocalService.getUser(id)

  def getByCourses(courseIds: Seq[Long],
                   companyId: Long,
                   organizationId: Option[Long] = None,
                   asc: Boolean = false): Seq[LUser] = {
    val filter = UserFilter(
      companyId = Some(companyId),
      organizationId = organizationId
    )
    val query = getQuery(filter)
    val userIds = courseIds.flatMap(userLocalService.getGroupUserIds).distinct
    addFilterByIds(query, userIds)
    userLocalService.dynamicQuery(query, asc, None)
  }

  def getByName(name: String, companyId: Long, count: Option[Int] = None) = {
    val users = all(companyId.toInt)
      .filter(_.getFullName.toLowerCase.contains(name.toLowerCase))
      .map(new User(_))

    count match {
      case Some(value) => users.take(value)
      case None => users
    }
  }

  def getByIds(companyId: Long, ids: Set[Long]): Seq[LUser] =
    userLocalService
      .getCompanyUsers(companyId, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .filter(user => ids.contains(user.getUserId))

  def getOrganizations: Seq[LOrganization] = {
    OrganizationLocalServiceHelper
      .getOrganizations(QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .asScala
  }

  override def all(companyId: Long): Seq[LUser] = getBy(UserFilter(Some(companyId)))

  def getUsersByGroupOrOrganization(companyId: Long, courseId: Long, organizationId: Option[Long]):Seq[LUser]={
    val filter = UserFilter(
      companyId = Some(companyId),
      groupId = Some(courseId),
      organizationId = organizationId
    )
    getBy(filter) match {
      case seq if seq.size > 0 => seq
      case _ =>
        val currentOrgazationId = organizationId.getOrElse(GroupLocalServiceHelper.getGroup(courseId).getOrganizationId())
        UserLocalServiceHelper().getOrganizationUsers(currentOrgazationId)
    }
  }
}
