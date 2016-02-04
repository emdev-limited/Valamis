package com.arcusys.learn.liferay.services

import java.util

import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.liferay.portal.kernel.dao.orm.{ProjectionFactoryUtil, RestrictionsFactoryUtil}
import com.liferay.portal.model.Group
import com.liferay.portal.service.{GroupLocalServiceUtil, ServiceContext}
import com.liferay.portal.util.comparator.GroupNameComparator
import com.liferay.portlet.asset.model.AssetVocabulary
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil

import scala.collection.JavaConverters._

object GroupLocalServiceHelper {
  def getGroup(groupId: Long): Group = GroupLocalServiceUtil.getGroup(groupId)

  def fetchGroup(groupId: Long): Group = GroupLocalServiceUtil.fetchGroup(groupId)

  def updateGroup(group: Group): Group = GroupLocalServiceUtil.updateGroup(group)

  def getCompanyGroup(companyId: Long): Group = GroupLocalServiceUtil.getCompanyGroup(companyId)

  def getCompanyGroups(companyId: Long, start: Int, end: Int): java.util.List[Group] =
    GroupLocalServiceUtil.getCompanyGroups(companyId, start, end)

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

    dq.setProjection(ProjectionFactoryUtil.projectionList()
      .add(ProjectionFactoryUtil.property("groupId")))

    GroupLocalServiceUtil.dynamicQuery(dq).asScala.map(_.asInstanceOf[Long])
  }

  def getGroups: java.util.List[Group] = GroupLocalServiceUtil.getGroups(QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)

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

  def updateFriendlyURL(groupId: Long, friendlyURL: String): Group =
    GroupLocalServiceUtil.updateFriendlyURL(groupId, friendlyURL)
}
