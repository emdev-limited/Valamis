package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.services.dynamicQuery._
import com.liferay.portal.kernel.dao.orm.{DynamicQuery, Order, OrderFactoryUtil, RestrictionsFactoryUtil}
import com.liferay.portal.model.UserGroup
import com.liferay.portal.service.UserGroupLocalServiceUtil

import scala.collection.JavaConverters.asScalaBufferConverter

/**
  * Created by mminin on 24.02.16.
  */
object UserGroupLocalServiceHelper {
  val NameKey = "name"
  val IdKey = "userGroupId"

  def getCount(userGroupIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String]): Long = {
    val query = dynamicQuery(userGroupIds, contains, companyId, nameLike)
    UserGroupLocalServiceUtil.dynamicQueryCount(query)
  }

  def getUserGroupsOfGroup(groupId: Long): Seq[UserGroup] = {
    UserGroupLocalServiceUtil.getGroupUserGroups(groupId).asScala
  }

  def getUserGroups(userGroupIds: Seq[Long],
                    contains: Boolean,
                    companyId: Long,
                    nameLike: Option[String],
                    ascending: Boolean,
                    startEnd: Option[(Int, Int)]): Seq[UserGroup] = {
    val order: (String => Order) = if (ascending) OrderFactoryUtil.asc else OrderFactoryUtil.desc
    val query = dynamicQuery(userGroupIds, contains, companyId, nameLike)
      .addOrder(order(NameKey))

    val (start, end) = startEnd.getOrElse((-1, -1))

    UserGroupLocalServiceUtil.dynamicQuery(query, start, end)
      .asScala.map(_.asInstanceOf[UserGroup])
  }


  def dynamicQuery(userGroupIds: Seq[Long],
                   contains: Boolean,
                   companyId: Long,
                   nameLike: Option[String]): DynamicQuery = {
    UserGroupLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq("companyId", companyId))
      .addLikeRestriction(NameKey, nameLike)
      .addFilterByValues(IdKey, userGroupIds, contains)
  }

  def addGroupUserGroups(courseId: Long, userGroupIds: Seq[Long]): Unit = {
    UserGroupLocalServiceUtil.addGroupUserGroups(courseId, userGroupIds.toArray)
  }

  def deleteGroupUserGroups(courseId: Long, userGroupIds: Seq[Long]): Unit = {
    UserGroupLocalServiceUtil.deleteGroupUserGroups(courseId, userGroupIds.toArray)
  }

  def getGroupUserGroups(groupId: Long): Seq[UserGroup] = {
    UserGroupLocalServiceUtil.getGroupUserGroups(groupId).asScala
  }

  def getGroupUserGroupsIds(groupId: Long): Seq[Long] = {
    UserGroupLocalServiceUtil.getGroupUserGroups(groupId).asScala.map(_.getUserGroupId)
  }
}
