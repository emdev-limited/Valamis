package com.arcusys.learn.liferay.services

import java.util.Locale

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.dynamicQuery._
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.kernel.util.{DigesterUtil, HttpUtil}
import com.liferay.portal.model.User
import com.liferay.portal.service.{ServiceContext, UserLocalServiceUtil}
import com.liferay.portal.webserver.WebServerServletTokenUtil

import scala.collection.JavaConverters._

object UserLocalServiceHelper {
  def apply() = new UserLocalServiceHelper {}
}

trait UserLocalServiceHelper {
  val IdKey = "userId"
  val FirstNameKey = "firstName"
  val LastNameKey = "lastName"

  def fetchUserByUuidAndCompanyId(name: String, companyId: Long): LUser = UserLocalServiceUtil.fetchUserByUuidAndCompanyId(name, companyId)

  def fetchUserByEmailAddress(companyId: Long, email: String): LUser = UserLocalServiceUtil.fetchUserByEmailAddress(companyId, email)

  def fetchUser(userId: Long): LUser = UserLocalServiceUtil.fetchUser(userId)

  def getUserByEmailAddress(companyId: Long, userEmail: String): LUser = UserLocalServiceUtil.getUserByEmailAddress(companyId, userEmail)

  def getUserById(userId: Long): LUser = UserLocalServiceUtil.getUserById(userId)

  def dynamicQuery: DynamicQuery = {
    UserLocalServiceUtil.dynamicQuery()
  }

  def dynamicQuery(dynamicQuery: DynamicQuery) = {
    UserLocalServiceUtil.dynamicQuery(dynamicQuery)
  }

  def dynamicQuery(dynamicQuery: DynamicQuery, start: Int, end: Int) = {
    UserLocalServiceUtil.dynamicQuery(dynamicQuery, start, end)
  }

  def dynamicQueryCount(dynamicQuery: DynamicQuery) = {
    UserLocalServiceUtil.dynamicQueryCount(dynamicQuery)
  }

  def getCount(userIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String],
               organizationId: Option[Long] = None): Long = {
    val query = dynamicQuery(userIds, contains, companyId, nameLike, organizationId)
    query.map(dynamicQueryCount).getOrElse(0L)
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

    dynamicQuery(q, start, end)
      .asScala.map(_.asInstanceOf[LUser])
  }

  private def dynamicQuery(userIds: Seq[Long],
                   contains: Boolean,
                   companyId: Long,
                   nameLike: Option[String],
                   organizationId: Option[Long]): Option[DynamicQuery] = {
    var query = UserLocalServiceHelper().dynamicQuery
      .add(RestrictionsFactoryUtil.eq("defaultUser", false))
      .add(RestrictionsFactoryUtil.eq("companyId", companyId))
      .addInSetRestriction(IdKey, userIds, contains)

    if (nameLike.isDefined) {
      query = query.add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.ilike(FirstNameKey, nameLike.get),
        RestrictionsFactoryUtil.ilike(LastNameKey, nameLike.get)
      ))
    }

    organizationId.map(getOrganizationUserIds) match {
      case None => Some(query)
      case Some(Nil) => None
      case Some(ids) => Some(query.add(RestrictionsFactoryUtil.in("userId", ids.asJava)))
    }
  }

  def getCompanyUsers(companyId: Long, start: Int, end: Int): java.util.List[User] =
    UserLocalServiceUtil.getCompanyUsers(companyId, start, end)

  def addGroupUsers(groupId: Long, userIds: Seq[Long]) {
    getOrganizationIdByGroupId(groupId) match {
      case Some(0) => UserLocalServiceUtil.addGroupUsers(groupId, userIds.toArray)
      case Some(id)  => UserLocalServiceUtil.addOrganizationUsers(id, userIds.toArray)
      case _ =>

    }
  }

  def getAllUsers(): Seq[LUser] = UserLocalServiceUtil.getUsers(-1, -1).asScala

  def getUsers(userIds: Seq[Long]): Seq[User] = {
    if (userIds.isEmpty) {
      Nil
    } else {
      val query = UserLocalServiceHelper().dynamicQuery
        .addInSetRestriction(IdKey, userIds, contains = true)

      UserLocalServiceHelper().dynamicQuery(query)
        .asScala.map(_.asInstanceOf[LUser])
    }
  }

  def getUser(userId: Long): LUser = UserLocalServiceUtil.getUser(userId)

  def getRoleUserIds(roleId: Long): Seq[Long] = UserLocalServiceUtil.getRoleUserIds(roleId)

  def getUserById(companyId: Long, userId: Long): User = UserLocalServiceUtil.getUserById(companyId, userId)

  def getRoleUsersCount(roleId: Long): Int = UserLocalServiceUtil.getRoleUsersCount(roleId)

  def getUsersByRoleId(liferayRoleId: Long): java.util.List[User] = UserLocalServiceUtil.getRoleUsers(liferayRoleId)

  def addGroupUsers(groupId: Long, userIds: Array[Long]) {
    getOrganizationIdByGroupId(groupId) match {
      case Some(0) => UserLocalServiceUtil.addGroupUsers(groupId, userIds)
      case Some(id) => UserLocalServiceUtil.addOrganizationUsers(id, userIds)
      case _ =>
    }
  }

  def deleteGroupUsers(groupId: Long, userIds: Array[Long]): Unit = {
    getOrganizationIdByGroupId(groupId) match {
      case Some(0) => UserLocalServiceUtil.deleteGroupUsers(groupId, userIds)
      case Some(id) => UserLocalServiceUtil.deleteOrganizationUsers(id, userIds)
      case _ =>
    }
  }

  def getGroupUsers(groupId: Long): Seq[LUser] = {
    UserLocalServiceUtil.getGroupUsers(groupId).asScala match {
      case seq if seq.size > 0 => seq
      case _ =>
        val orgId = getOrganizationIdByGroupId(groupId)
        orgId match {
          case Some(id) => UserLocalServiceUtil.getOrganizationUsers(id).asScala
          case None => Seq()
        }
    }
  }

  def getGroupUserIds(groupId: Long): Seq[Long] = {
    UserLocalServiceUtil.getGroupUserIds(groupId) match {
      case seq if seq.size > 0 => seq
      case _ =>
        val orgId = getOrganizationIdByGroupId(groupId)
        orgId match {
          case Some(id) => UserLocalServiceUtil.getOrganizationUserIds(id)
          case None => Seq()
        }
    }
  }

  def getOrganizationUserIds(orgId: Long): Seq[Long] =
    UserLocalServiceUtil.getOrganizationUserIds(orgId)

  def getOrganizationUsers(orgId: Long): Seq[User] =
    UserLocalServiceUtil.getOrganizationUsers(orgId).asScala

  def getRoleUsers(roleId: Long): Seq[User] =
    UserLocalServiceUtil.getRoleUsers(roleId).asScala

  def getDefaultUserId(companyId: Long): Long = UserLocalServiceUtil.getDefaultUserId(companyId)

  def getDefaultUser(companyId: Long): LUser = UserLocalServiceUtil.getDefaultUser(companyId)

  def unsetOrganizationUsers(organizationId: Long, userIds: Array[Long]) {
    UserLocalServiceUtil.unsetOrganizationUsers(organizationId, userIds)
  }

  def addUser(creatorUserId: Long, companyId: Long, autoPassword: Boolean,
    password1: String, password2: String,
    autoScreenName: Boolean, screenName: String, emailAddress: String,
    facebookId: Long, openId: String, locale: Locale,
    firstName: String, middleName: String, lastName: String,
    prefixId: Int, suffixId: Int, male: Boolean,
    birthdayMonth: Int, birthdayDay: Int, birthdayYear: Int,
    jobTitle: String, groupIds: Array[Long], organizationIds: Array[Long],
    roleIds: Array[Long], userGroupIds: Array[Long], sendEmail: Boolean,
    serviceContext: ServiceContext): User =
    UserLocalServiceUtil.addUser(creatorUserId, companyId, autoPassword, password1, password2,
      autoScreenName, screenName, emailAddress, facebookId, openId, locale,
      firstName, middleName, lastName, prefixId, suffixId, male,
      birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds,
      roleIds, userGroupIds, sendEmail, serviceContext)

  def updatePortrait(userId: Long, bytes: Array[Byte]): User = UserLocalServiceUtil.updatePortrait(userId, bytes)

  def updateReminderQuery(userId: Long, question: String, answer: String): User =
    UserLocalServiceUtil.updateReminderQuery(userId, question, answer)

  def getPortraitTime(portraitId: Long) = {
    WebServerServletTokenUtil.getToken(portraitId)
  }
  def getPortraitToken(user: User) = {
    HttpUtil.encodeURL(DigesterUtil.digest(user.getUserUuid))
  }

  def getUsersCount = {
    UserLocalServiceUtil.getUsersCount
  }

  def getOrganizationIdByGroupId(groupId: Long): Option[Long] ={
    Option(GroupLocalServiceHelper.fetchGroup(groupId)).map(_.getOrganizationId)
  }
}
