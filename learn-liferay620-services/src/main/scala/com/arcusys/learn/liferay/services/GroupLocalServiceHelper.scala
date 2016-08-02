package com.arcusys.learn.liferay.services

import java.util

import com.arcusys.learn.liferay.LiferayClasses.{LGroup, LUser}
import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.liferay.portal.kernel.dao.orm.{ProjectionFactoryUtil, QueryUtil, RestrictionsFactoryUtil}
import com.liferay.portal.model._
import com.liferay.portal.service._
import com.liferay.portal.util.comparator.GroupNameComparator
import com.liferay.portlet.asset.model.AssetVocabulary
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil

import scala.collection.JavaConverters._

object GroupLocalServiceHelper {

  val TYPE_SITE_OPEN = GroupConstants.TYPE_SITE_OPEN
  val TYPE_SITE_RESTRICTED = GroupConstants.TYPE_SITE_RESTRICTED
  val TYPE_SITE_PRIVATE = GroupConstants.TYPE_SITE_PRIVATE

  def getCompanyGroupsCount(companyId: Long) = GroupLocalServiceUtil.getCompanyGroupsCount(companyId)

  def getGroup(groupId: Long): Group = GroupLocalServiceUtil.getGroup(groupId)

  def fetchGroup(groupId: Long): Group = GroupLocalServiceUtil.fetchGroup(groupId)

  def updateGroup(group: Group): Group = GroupLocalServiceUtil.updateGroup(group)

  def deleteGroup(groupId: Long): Unit = GroupLocalServiceUtil.deleteGroup(groupId)

  def getCompanyGroup(companyId: Long): Group = GroupLocalServiceUtil.getCompanyGroup(companyId)

  def getCompanyGroups(companyId: Long, start: Int, end: Int): java.util.List[Group] =
    GroupLocalServiceUtil.getCompanyGroups(companyId, start, end)

  def getUserSitesGroups(userId: Long): util.List[Group] =
    GroupLocalServiceUtil.getUserSitesGroups(userId)

  def getCompanyGroupIdsActiveSites(companyId: Long, start: Int, end: Int): Seq[Long] = {
    val dq = GroupLocalServiceUtil.dynamicQuery()
    dq.add(RestrictionsFactoryUtil.eq("companyId", companyId))
      .add(RestrictionsFactoryUtil.eq("site", true))
      .add(RestrictionsFactoryUtil.eq("active", true))
      .add(RestrictionsFactoryUtil.ne("friendlyURL", "/control_panel"))

    dq.setProjection(ProjectionFactoryUtil.projectionList()
      .add(ProjectionFactoryUtil.property("groupId")))

    GroupLocalServiceUtil.dynamicQuery(dq, start, end).asScala.map(_.asInstanceOf[Long])
  }

  def getGroupIdsForAllActiveSites: Seq[Long] = {
    val dq = GroupLocalServiceUtil.dynamicQuery()
    dq.add(RestrictionsFactoryUtil.eq("site", true))
      .add(RestrictionsFactoryUtil.eq("active", true))
      .add(RestrictionsFactoryUtil.ne("friendlyURL", "/control_panel"))
      .add(RestrictionsFactoryUtil.ne("friendlyURL", "/guest"))

    dq.setProjection(ProjectionFactoryUtil.projectionList()
      .add(ProjectionFactoryUtil.property("groupId")))

    GroupLocalServiceUtil.dynamicQuery(dq).asScala.map(_.asInstanceOf[Long])
  }

  def getGroups: java.util.List[Group] = GroupLocalServiceUtil.getGroups(QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)

  def getSiteGroupsByUser(user: LUser) : Seq[LGroup] = {
    user.getMySiteGroups(false, -1).asScala
  }

  def getGroupsByUserId(userId: Long): java.util.List[Group] = GroupLocalServiceUtil.getUserGroups(userId)

  def getGroupsByUserId(userId: Long, skip: Int, take: Int, sortAsc: Boolean = true): java.util.List[Group] =
    GroupLocalServiceUtil.getUserGroups(userId, skip, take, new GroupNameComparator(sortAsc))

  def getGroupsCountByUserId(userId: Long): Long =
    GroupLocalServiceUtil.getUserGroupsCount(userId)

  def getGroupVocabulary(globalGroupId: Long, vocabularyName: String): AssetVocabulary =
    AssetVocabularyLocalServiceUtil.getGroupVocabulary(globalGroupId, vocabularyName)

  def addGroupVocabulary(userId: Long, title: String, context: ServiceContext) = {
    AssetVocabularyLocalServiceUtil.addVocabulary(userId, title, context)
  }

  def search(companyId: Long,
    classNameIds: Array[Long],
    keywords: String,
    params: util.LinkedHashMap[String, AnyRef],
    start: Int,
    end: Int): java.util.List[Group] =
    GroupLocalServiceUtil.search(companyId, classNameIds, keywords: String, params, start, end)

  def searchExceptPrivateSites(companyId: Long,
    start: Int,
    end: Int): Seq[Group] =
    GroupLocalServiceUtil.getCompanyGroups(companyId, start, end).
      asScala.
      filterNot(g => g.isUser || g.isUserGroup || g.isUserPersonalSite)

  def searchIdsExceptPrivateSites(companyId: Long,
                                  start: Int = QueryUtilHelper.ALL_POS,
                                  end: Int = QueryUtilHelper.ALL_POS): Seq[Long] = {
    GroupLocalServiceUtil.getCompanyGroups(companyId, start, end)
      .asScala
      .filterNot(g => g.isUser || g.isUserGroup || g.isUserPersonalSite)
      .map(_.getGroupId)
  }

  def updateFriendlyURL(groupId: Long, friendlyURL: String): Group =
    GroupLocalServiceUtil.updateFriendlyURL(groupId, friendlyURL)

  def addPublicSite(userId: Long, title: String, description: Option[String], friendlyUrl: Option[String], groupType : Int, isActive: Boolean = true, tags: Seq[String]): Group = {
    val parentGroupId = GroupConstants.DEFAULT_PARENT_GROUP_ID
    val liveGroupId = GroupConstants.DEFAULT_LIVE_GROUP_ID
    val membershipRestriction = GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION
    val manualMembership = true
    val isSite = true
    val defaultPageTitle = "home"
    val defaultPageUrl = "/home"

    val serviceContext: ServiceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val siteFriendlyUrl = friendlyUrl match {
      case Some(url) => if(url.startsWith("/")) url else "/" + url
      case None => "/" + title.trim.toLowerCase.replace(' ', '-')
    }

    val group = GroupLocalServiceUtil.addGroup(
      userId,
      parentGroupId,
      classOf[Group].getName,
      0, //classPK
      liveGroupId,
      title,
      description.getOrElse(""),
      groupType,
      manualMembership,
      membershipRestriction,
      siteFriendlyUrl,
      isSite,
      isActive,
      //tags,
      serviceContext)

    val newSiteGropeId = group.getGroupId

    //TODO what theme we will use?
    //    val themeId = "valamistheme_WAR_valamistheme"
    //    setupTheme(newSiteGropeId, themeId)

    addLayout(newSiteGropeId, userId, defaultPageTitle, defaultPageUrl)

    // addAllUsersToSite(newSiteGropeId)

    group
  }

  private def addAllUsersToSite(siteGroupId:Long) = {
    val allUsers = UserLocalServiceUtil.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS).asScala

    val roles = RoleLocalServiceUtil.getTypeRoles(RoleConstants.TYPE_SITE).asScala
    val memberRole = roles.filter(role => role.getName.equals(RoleConstants.SITE_MEMBER)).head

    val userIds = allUsers.map(user => user.getUserId).toArray
    UserGroupRoleLocalServiceUtil.addUserGroupRoles(userIds, siteGroupId, memberRole.getRoleId)
  }

  private def setupTheme(siteGroupId: Long, themeId: String): LayoutSet = {
    LayoutSetLocalServiceUtil
      .updateLookAndFeel(siteGroupId, false, themeId, "", "", false)

    LayoutSetLocalServiceUtil
      .updateLookAndFeel(siteGroupId, true, themeId, "", "", false)
  }

  private def addLayout(siteGroupId: Long, userId: Long, layoutName: String, layoutUrl: String): Layout = {

    val serviceContext: ServiceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val isLayoutPrivate = false
    val isHidden = false
    val parentLayout = LayoutConstants.DEFAULT_PARENT_LAYOUT_ID
    val title = layoutName
    val description = ""
    val layoutFriendlyURL = layoutUrl
    val layoutType = LayoutConstants.TYPE_PORTLET

    LayoutLocalServiceUtil.addLayout(
      userId,
      siteGroupId,
      isLayoutPrivate,
      parentLayout,
      layoutName,
      title,
      description,
      layoutType,
      isHidden, //hidden
      layoutFriendlyURL,
      serviceContext
    )
  }
}
