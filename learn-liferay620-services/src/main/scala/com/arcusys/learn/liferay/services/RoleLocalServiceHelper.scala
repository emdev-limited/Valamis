package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.services.dynamicQuery._
import com.liferay.portal.kernel.dao.orm.{DynamicQuery, Order, OrderFactoryUtil, RestrictionsFactoryUtil}
import com.liferay.portal.model.{RoleConstants, Role}
import com.liferay.portal.service.RoleLocalServiceUtil

import scala.collection.JavaConverters.asScalaBufferConverter

/**
  * Created by mminin on 24.02.16.
  */
object RoleLocalServiceHelper {
  val NameKey = "name"
  val IdKey = "roleId"
  val CompanyIdKey = "companyId"
  val RoleType = "type"

  val RoleTypeRegular = RoleConstants.TYPE_REGULAR

  val RoleSiteOwner = RoleConstants.SITE_OWNER

  def getCount(roleIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String],
               roleType: Int): Long = {
    val query = dynamicQuery(roleIds, contains, companyId, nameLike, roleType)
    RoleLocalServiceUtil.dynamicQueryCount(query)
  }

  def getRoles(roleIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String],
               roleType: Int,
               ascending: Boolean,
               startEnd: Option[(Int, Int)]): Seq[Role] = {
    val order: (String => Order) = if (ascending) OrderFactoryUtil.asc else OrderFactoryUtil.desc

    val query = dynamicQuery(roleIds, contains, companyId, nameLike, roleType)
      .addOrder(order(NameKey))

    val (start, end) = startEnd.getOrElse((-1, -1))

    RoleLocalServiceUtil.dynamicQuery(query, start, end)
      .asScala.map(_.asInstanceOf[Role])
  }

  def getRole(companyId: Long, name: String): Role = RoleLocalServiceUtil.getRole(companyId, name)


  def dynamicQuery(roleIds: Seq[Long],
                   contains: Boolean,
                   companyId: Long,
                   nameLike: Option[String],
                   roleType: Int): DynamicQuery = {
    RoleLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq(CompanyIdKey, companyId))
      .add(RestrictionsFactoryUtil.eq(RoleType, roleType))
      .addLikeRestriction(NameKey, nameLike)
      .addFilterByValues(IdKey, roleIds, contains)
  }
}
