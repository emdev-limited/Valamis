package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.{LAssetVocabulary, LGroup, LUser}
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.model._
import com.liferay.portal.service._
import com.liferay.portal.util.comparator.GroupNameComparator
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil

import scala.collection.JavaConverters._

object GroupLocalServiceHelper {

  val TYPE_SITE_OPEN = GroupConstants.TYPE_SITE_OPEN
  val TYPE_SITE_RESTRICTED = GroupConstants.TYPE_SITE_RESTRICTED
  val TYPE_SITE_PRIVATE = GroupConstants.TYPE_SITE_PRIVATE

  def getGroup(groupId: Long): LGroup = {
    new LGroup(GroupLocalServiceUtil.getGroup(groupId))
  }

  def fetchGroup(groupId: Long): Option[LGroup] = {
    Option(GroupLocalServiceUtil.fetchGroup(groupId)).map(new LGroup(_))
  }

  def updateGroup(group: LGroup): LGroup = {
    new LGroup(GroupLocalServiceUtil.updateGroup(group.group))
  }

  def deleteGroup(groupId: Long): Unit = {
    GroupLocalServiceUtil.deleteGroup(groupId)
  }

  def getCompanyGroup(companyId: Long): LGroup = {
    new LGroup(GroupLocalServiceUtil.getCompanyGroup(companyId))
  }

  def getCompanyGroups(companyId: Long,
                       start: Int = QueryUtil.ALL_POS,
                       end: Int = QueryUtil.ALL_POS): Seq[LGroup] = {
    GroupLocalServiceUtil.getCompanyGroups(companyId, start, end).asScala
      .map(new LGroup(_))
  }

  def getUserSitesGroups(userId: Long): Seq[LGroup] = {
    GroupLocalServiceUtil.getUserSitesGroups(userId).asScala
      .map(new LGroup(_))
  }

  def getSiteGroupsByUser(user: LUser): Seq[LGroup] = {
    user.getMySiteGroups(false, -1).asScala
      .map(new LGroup(_))
  }

  def getGroupsByUserId(userId: Long): Seq[LGroup] = {
    GroupLocalServiceUtil.getUserGroups(userId).asScala
      .map(new LGroup(_))
  }

  def getGroupsByUserId(userId: Long, skip: Int, take: Int, sortAsc: Boolean = true): Seq[LGroup] = {
    GroupLocalServiceUtil.getUserGroups(userId, skip, take, new GroupNameComparator(sortAsc)).asScala
      .map(new LGroup(_))
  }

  def getGroupsCountByUserId(userId: Long): Long = {
    GroupLocalServiceUtil.getUserGroupsCount(userId)
  }

  def getGroupVocabulary(globalGroupId: Long, vocabularyName: String): LAssetVocabulary = {
    AssetVocabularyLocalServiceUtil.getGroupVocabulary(globalGroupId, vocabularyName)
  }

  def searchExceptPrivateSites(companyId: Long,
                               start: Int,
                               end: Int): Seq[LGroup] = {
    GroupLocalServiceUtil.getCompanyGroups(companyId, start, end)
      .asScala
      .filterNot(g => g.isUser || g.isUserGroup || g.isUserPersonalSite)
      .map(new LGroup(_))
  }

  def searchIdsExceptPrivateSites(companyId: Long,
                                  start: Int = QueryUtil.ALL_POS,
                                  end: Int = QueryUtil.ALL_POS): Seq[Long] = {
    GroupLocalServiceUtil.getCompanyGroups(companyId, start, end)
      .asScala
      .filterNot(g => g.isUser || g.isUserGroup || g.isUserPersonalSite)
      .map(_.getGroupId)
  }

  def addPublicSite(userId: Long,
                    title: String,
                    description: Option[String],
                    friendlyUrl: String,
                    groupType: Int,
                    isActive: Boolean = true,
                    tags: Seq[String],
                    companyId: Long): LGroup = {
    val parentGroupId = GroupConstants.DEFAULT_PARENT_GROUP_ID
    val liveGroupId = GroupConstants.DEFAULT_LIVE_GROUP_ID
    val membershipRestriction = GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION
    val manualMembership = true
    val isSite = true
    val serviceContext: ServiceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)


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
      friendlyUrl,
      isSite,
      isActive,
      serviceContext)

    new LGroup(group)
  }

  def addLayout(siteGroupId: Long, userId: Long, layoutName: String, layoutUrl: String, isPrivate: Boolean): Layout = {

    val serviceContext: ServiceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val isHidden = false
    val parentLayout = LayoutConstants.DEFAULT_PARENT_LAYOUT_ID
    val title = layoutName
    val description = ""
    val layoutFriendlyURL = layoutUrl
    val layoutType = LayoutConstants.TYPE_PORTLET

    val layout = LayoutLocalServiceUtil.addLayout(
      userId,
      siteGroupId,
      isPrivate,
      parentLayout,
      layoutName,
      title,
      description,
      layoutType,
      isHidden, //hidden
      layoutFriendlyURL,
      serviceContext
    )

    val valamisLayoutPage = "ValamisDefaultPage"
    val themeLayouts = LayoutTemplateLocalServiceUtil.getLayoutTemplates(layout.getTheme.getThemeId).asScala
    if (themeLayouts.exists(_.getLayoutTemplateId == valamisLayoutPage)) {
      val layoutTypeUpdate = layout.getLayoutType.asInstanceOf[LayoutTypePortlet]

      layoutTypeUpdate.setLayoutTemplateId(userId, valamisLayoutPage)
      LayoutLocalServiceUtil.updateLayout(layout)
    } else layout

  }

  def setThemeToLayout(groupId: Long, themeId: String): Unit = {
    LayoutSetLocalServiceHelper.updateLookAndFeel(
      groupId,
      themeId,
      colorSchemeId = null,
      css = null)
  }
}
