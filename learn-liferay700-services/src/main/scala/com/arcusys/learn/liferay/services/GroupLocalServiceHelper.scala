package com.arcusys.learn.liferay.services

import java.util

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.kernel.model._
import com.liferay.portal.kernel.service._
import com.liferay.portal.kernel.util.LocaleUtil
import com.liferay.portal.kernel.util.comparator.GroupNameComparator

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
    user.getSiteGroups(false).asScala
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

  def searchSites(companyId: Long,
                  start: Int,
                  end: Int,
                  includeOpen: Boolean = true,
                  includeRestricted: Boolean = true,
                  includePrivate: Boolean = true
                 ): Seq[Group] = {
    val siteParams = new util.LinkedHashMap[String, AnyRef](3)

    siteParams.put("site", Boolean.box(true))
    siteParams.put("active", Boolean.box(true))

    val types = Seq(
      (includeOpen, GroupConstants.TYPE_SITE_OPEN),
      (includeRestricted, GroupConstants.TYPE_SITE_RESTRICTED),
      (includePrivate, GroupConstants.TYPE_SITE_PRIVATE))
      .filter(_._1)
      .map(_._2)

    siteParams.put("types", types.asJava)

    GroupLocalServiceUtil
      .search(companyId, "", siteParams, start, end)
      .asScala
  }

  def searchSiteIds(companyId: Long,
                    start: Int = QueryUtilHelper.ALL_POS,
                    end: Int = QueryUtilHelper.ALL_POS): Seq[Long] = {
    searchSites(companyId, start, end)
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
    val defaultPageTitle = "Home"
    val defaultPageUrl = "/home"

    val serviceContext: ServiceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val group = GroupLocalServiceUtil.addGroup(
      userId,
      parentGroupId,
      classOf[Group].getName,
      0, //classPK
      liveGroupId,
      Map(LocaleUtil.getDefault -> title).asJava,
      Map(LocaleUtil.getDefault -> description.getOrElse("")).asJava,
      groupType,
      manualMembership,
      membershipRestriction,
      friendlyUrl,
      isSite,
      false,
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
    val layout: LLayout = LayoutLocalServiceUtil.addLayout(
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