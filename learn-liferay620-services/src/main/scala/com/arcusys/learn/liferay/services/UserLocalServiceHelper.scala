package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.{LDynamicQuery, LUser}
import com.arcusys.learn.liferay.services.dynamicQuery._
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.kernel.search.{Field, Sort}
import com.liferay.portal.kernel.util.GetterUtil
import com.liferay.portal.kernel.workflow.WorkflowConstants
import com.liferay.portal.service.UserLocalServiceUtil

import scala.collection.JavaConverters._

object UserLocalServiceHelper {
  def apply() = new UserLocalServiceHelper {}
}

trait UserLocalServiceHelper {
  val IdKey = "userId"
  val FirstNameKey = "firstName"
  val LastNameKey = "lastName"

  def fetchUserByUuidAndCompanyId(uuid: String, companyId: Long): LUser = {
    UserLocalServiceUtil.fetchUserByUuidAndCompanyId(uuid, companyId)
  }

  def fetchUserByEmailAddress(companyId: Long, email: String): LUser = {
    UserLocalServiceUtil.fetchUserByEmailAddress(companyId, email)
  }

  def fetchUser(userId: Long): Option[LUser] = {
    Option(UserLocalServiceUtil.fetchUser(userId))
  }

  def hasUser(userId: Long): Boolean = {
    fetchUser(userId).isDefined
  }

  def getUser(userId: Long): LUser = {
    UserLocalServiceUtil.getUser(userId)
  }

  def dynamicQuery: LDynamicQuery = {
    UserLocalServiceUtil.dynamicQuery()
  }

  def dynamicQuery(dynamicQuery: DynamicQuery): Seq[Any] = {
    UserLocalServiceUtil.dynamicQuery(dynamicQuery).asScala
  }

  def dynamicQuery(dynamicQuery: DynamicQuery, start: Int, end: Int): Seq[Any] = {
    UserLocalServiceUtil.dynamicQuery(dynamicQuery, start, end).asScala
  }

  def dynamicQueryCount(dynamicQuery: DynamicQuery): Long = {
    UserLocalServiceUtil.dynamicQueryCount(dynamicQuery)
  }

  def getCount(userIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String],
               organizationId: Option[Long] = None): Long = {
    val query = dynamicQuery(userIds, contains, companyId, nameLike, organizationId)
    query.map(UserLocalServiceUtil.dynamicQueryCount).getOrElse(0L)
  }

  def getUsers(userIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String],
               ascending: Boolean,
               startEnd: Option[(Int, Int)],
               organizationId: Option[Long] = None): Seq[LUser] = {
    val query = dynamicQuery(userIds, contains, companyId, nameLike, organizationId)

    query.map(q => dynamicQuery(q, ascending, startEnd)).getOrElse(Nil)
  }

  def dynamicQuery(query: DynamicQuery,
                   ascending: Boolean,
                   startEnd: Option[(Int, Int)]): Seq[LUser] = {
    val (start, end) = startEnd.getOrElse((-1, -1))

    val q = if (ascending) {
      query.addOrder(OrderFactoryUtil.asc(FirstNameKey)).addOrder(OrderFactoryUtil.asc(LastNameKey))
    } else {
      query.addOrder(OrderFactoryUtil.desc(FirstNameKey)).addOrder(OrderFactoryUtil.desc(LastNameKey))
    }

    UserLocalServiceUtil.dynamicQuery(q, start, end)
      .asScala.map(_.asInstanceOf[LUser])
  }

  private def dynamicQuery(userIds: Seq[Long],
                           contains: Boolean,
                           companyId: Long,
                           nameLike: Option[String],
                           organizationId: Option[Long],
                           activated: Option[Boolean]= Some(true)): Option[DynamicQuery] = {
    var query = UserLocalServiceUtil.dynamicQuery
      .add(RestrictionsFactoryUtil.eq("defaultUser", false))
      .add(RestrictionsFactoryUtil.eq("companyId", companyId))
      .addFilterByValues(IdKey, userIds, contains)

    if (nameLike.isDefined) {
      query = query.add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.ilike(FirstNameKey, nameLike.get),
        RestrictionsFactoryUtil.ilike(LastNameKey, nameLike.get)
      ))
    }

    organizationId.map(getOrganizationUserIds) match {
      case None => Some(query)
      case Some(Nil) => None
      case Some(ids) => Some(query.addFilterByValues("userId", ids, contains = true))
    }

    val deactivedStatus = 5
    activated match{
      case Some(true) => Some(query.add(RestrictionsFactoryUtil.ne("status", deactivedStatus)))
      case Some(false) => Some(query.add(RestrictionsFactoryUtil.eq("status", deactivedStatus)))
      case _ =>  Some(query)
    }
  }

  def getCompanyUsers(companyId: Long,
                      start: Int = QueryUtil.ALL_POS,
                      end: Int = QueryUtil.ALL_POS): Seq[LUser] = {
    UserLocalServiceUtil.getCompanyUsers(companyId, start, end).asScala
  }

  def addGroupUsers(groupId: Long, userIds: Seq[Long]) {
    getOrganizationIdByGroupId(groupId) match {
      case Some(0) => UserLocalServiceUtil.addGroupUsers(groupId, userIds.toArray)
      case Some(id) => UserLocalServiceUtil.addOrganizationUsers(id, userIds.toArray)
      case _ =>
    }
  }

  def addUser(groupId: Long, userId: Long) {
    getOrganizationIdByGroupId(groupId) match {
      // UserLocalServiceUtil.addGroupUser() doesn't clear permission check.
      case Some(0) => UserLocalServiceUtil.addGroupUsers(groupId, Set(userId).toArray)
      case Some(id) => UserLocalServiceUtil.addOrganizationUser(groupId, userId)
      case _ =>
    }
  }

  def getUsers(userIds: Seq[Long]): Seq[LUser] = {
    if (userIds.isEmpty) {
      Nil
    } else {
      val query = UserLocalServiceUtil.dynamicQuery
        .addFilterByValues(IdKey, userIds, contains = true)

      UserLocalServiceUtil.dynamicQuery(query)
        .asScala.map(_.asInstanceOf[LUser])
    }
  }

  def getActiveUsers(userIds: Seq[Long]): Seq[LUser] = {
    if (userIds.nonEmpty) {
      val query = UserLocalServiceUtil.dynamicQuery()
        .add(RestrictionsFactoryUtil.eq("defaultUser", false))
        .add(RestrictionsFactoryUtil.eq("status", 0))
        .addFilterByValues(IdKey, userIds, contains = true)

      UserLocalServiceUtil.dynamicQuery(query)
        .asScala.map(_.asInstanceOf[LUser])
    } else Nil
  }

  def getActiveUsers(userIds: Seq[Long], companyId: Long, contains: Boolean, asc: Boolean): Seq[LUser] = {
    val query = UserLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq("defaultUser", false))
      .add(RestrictionsFactoryUtil.eq("companyId", companyId))
      .add(RestrictionsFactoryUtil.eq("status", WorkflowConstants.STATUS_APPROVED))
      .addFilterByValues(IdKey, userIds, contains)

    dynamicQuery(query, asc, None)
  }

  def getRoleUserIds(roleId: Long): Seq[Long] = UserLocalServiceUtil.getRoleUserIds(roleId)

  def deleteGroupUsers(groupId: Long, userIds: Seq[Long]): Unit = {
    getOrganizationIdByGroupId(groupId) match {
      case Some(0) => UserLocalServiceUtil.deleteGroupUsers(groupId, userIds.toArray)
      case Some(id) => UserLocalServiceUtil.deleteOrganizationUsers(id, userIds.toArray)
      case _ =>
    }
  }

  def getGroupUsers(groupId: Long): Seq[LUser] = {
    UserLocalServiceUtil.getGroupUsers(groupId).asScala match {
      case seq if seq.nonEmpty => seq
      case _ =>
        val orgId = getOrganizationIdByGroupId(groupId)
        orgId match {
          case Some(id) => UserLocalServiceUtil.getOrganizationUsers(id).asScala
          case None => Seq()
        }
    }
  }

  def getGroupUserIds(groupId: Long): Seq[Long] = {
    val usersIds = UserLocalServiceUtil.getGroupUsers(groupId)
      .asScala.filter(_.isActive)
      .map(_.getUserId)

    usersIds match {
      case seq if seq.nonEmpty => seq
      case _ =>
        val orgId = getOrganizationIdByGroupId(groupId)
        orgId match {
          case Some(id) => UserLocalServiceUtil.getOrganizationUserIds(id)
          case None => Seq()
        }
    }
  }

  def getUserGroupUsers(userGroupId: Long): Seq[LUser] = {
    UserLocalServiceUtil.getUserGroupUsers(userGroupId).asScala
  }

  def getUserGroupUsersIds(userGroupId: Long): Seq[Long] = {
    UserLocalServiceUtil.getUserGroupUsers(userGroupId).asScala.map(_.getUserId)
  }

  def getOrganizationUserIds(orgId: Long): Seq[Long] =
    UserLocalServiceUtil.getOrganizationUserIds(orgId)

  def getOrganizationUsers(orgId: Long): Seq[LUser] =
    UserLocalServiceUtil.getOrganizationUsers(orgId).asScala

  def getRoleUsers(roleId: Long): Seq[LUser] =
    UserLocalServiceUtil.getRoleUsers(roleId).asScala

  def getDefaultUserId(companyId: Long): Long = UserLocalServiceUtil.getDefaultUserId(companyId)

  def getDefaultUser(companyId: Long): LUser = UserLocalServiceUtil.getDefaultUser(companyId)

  def getUsersCount: Int = {
    UserLocalServiceUtil.getUsersCount
  }

  private def getOrganizationIdByGroupId(groupId: Long): Option[Long] = {
    GroupLocalServiceHelper.fetchGroup(groupId).map(_.getOrganizationId)
  }

  def searchByName(companyId: Long,
                   namePattern: String,
                   skipOpt: Option[Int] = None,
                   takeOpt: Option[Int] = None): Seq[LUser] = {
    val indexerParameters = null
    val sort = new Sort("lastName", Sort.STRING_TYPE, true)


    val (skip, take) = (skipOpt, takeOpt) match {
      case (None, Some(take)) =>  (0, take)
      case _ => (skipOpt getOrElse QueryUtil.ALL_POS, takeOpt getOrElse QueryUtil.ALL_POS)
    }

    val hits = UserLocalServiceUtil.search(
      companyId,
      namePattern,
      WorkflowConstants.STATUS_APPROVED,
      indexerParameters,
      skip,
      take,
      sort
    )

    for (document <- hits.toList.asScala) yield {
      val userId = GetterUtil.getLong(document.get(Field.USER_ID))
      UserLocalServiceUtil.getUserById(userId)
    }
  }
}