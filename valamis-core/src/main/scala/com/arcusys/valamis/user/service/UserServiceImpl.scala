package com.arcusys.valamis.user.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.arcusys.learn.liferay.services.{OrganizationLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.valamis.model.{SkipTake, Order}
import com.arcusys.valamis.user.model._
import com.arcusys.valamis.user.storage.UserStorage
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.kernel.workflow.WorkflowConstants
import com.liferay.portal.model.Organization

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
// TODO: refactor (get by id)
class UserServiceImpl(implicit val bindingModule: BindingModule)
  extends UserService
  with UserConverter
  with Injectable {

  private lazy val userStorage = inject[UserStorage]
  private lazy val userCertificateRepository = inject[UserCertificateRepository]
  private lazy val userLocalService = UserLocalServiceHelper()

  def all(courseId: Long, namePart: String, sortAZ: Boolean): Seq[LUser] =
    getBy(UserFilter(
      namePart = namePart,
      groupId = Some(courseId),
      sortBy = Some(UserSort(UserSortBy.Name, Order.apply(sortAZ)))
    ))

  def getBy(filter: UserFilter, skipTake: Option[SkipTake]) = {
    val query = getQuery(filter)

    filter.sortBy match {
      case Some(UserSort(_, Order.Asc)) =>
        query.addOrder(OrderFactoryUtil.asc("lastName"))
          .addOrder(OrderFactoryUtil.asc("firstName"))
      case _ =>
        query.addOrder(OrderFactoryUtil.desc("lastName"))
          .addOrder(OrderFactoryUtil.desc("firstName"))
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
      .add(
        RestrictionsFactoryUtil.or(
          RestrictionsFactoryUtil.ne("firstName", ""),
          RestrictionsFactoryUtil.ne("lastName", "")
        )
      )
    
    for(companyId <- filter.companyId)
      query.add(RestrictionsFactoryUtil.eq("companyId", companyId))
    
    if(filter.namePart.nonEmpty) {
      query.add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.ilike("firstName", "%" + filter.namePart + "%"),
        RestrictionsFactoryUtil.ilike("lastName", "%" + filter.namePart + "%")
      ))
    }

    for (certificateId <- filter.certificateId) {
      addFilterByIds(query, userCertificateRepository.getUsersBy(certificateId), filter.isUserJoined)
    }

    if (filter.groupId.isDefined || filter.organizationId.isDefined) {
      val organizations =if(filter.groupId.isDefined)
        //Find organizations that is member of that site
         OrganizationLocalServiceHelper.getGroupOrganizations(filter.groupId.get).map(_.getOrganizationId)
      else
         Seq()

      val userIds = filter.groupId
        .map(userLocalService.getGroupUserIds).getOrElse(Seq()) ++
              filter.organizationId.map(userLocalService.getOrganizationUserIds).getOrElse(Seq()) ++
              organizations.flatMap(userLocalService.getOrganizationUserIds)
        .distinct

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

  def getByName(name: String, companyId: Long, count: Option[Int] = None) = {
    val users = all(companyId.toInt)
      .filter(_.getFullName.toLowerCase.contains(name.toLowerCase))
      .map(toModel)

    count match {
      case Some(value) => users.take(value)
      case None => users
    }
  }

  def getByIds(companyId: Long, ids: Set[Long]): Seq[LUser] =
    userLocalService
      .getCompanyUsers(companyId, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .filter(user => ids.contains(user.getUserId))

  def getOrganizations: Seq[Organization] = {
    OrganizationLocalServiceHelper
      .getOrganizations(QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .asScala
  }

  override def all(companyId: Long): Seq[LUser] = getBy(UserFilter(Some(companyId)))

  def getUserOption(userId: Int): Option[ScormUser] = {
    userStorage.getByID(userId)
  }

  // TODO rename
  def createAndGetId(userId: Int, name: String): Unit = {
    userStorage.createAndGetID(new ScormUser(userId, name))
  }
}

trait UserConverter {
  def toModel(lUser: LUser) = User(lUser.getUserId, lUser.getFullName)
}
