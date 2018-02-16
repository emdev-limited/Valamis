package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.LOrganization
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.kernel.model.Organization
import com.liferay.portal.kernel.service.{OrganizationLocalServiceUtil, ServiceContext}

import scala.collection.JavaConverters._
import com.arcusys.learn.liferay.services.dynamicQuery._

object OrganizationLocalServiceHelper {
  val NameKey = "name"
  val IdKey = "organizationId"

  def getOrganizations(start: Int = QueryUtil.ALL_POS,
                       end: Int = QueryUtil.ALL_POS): Seq[LOrganization] = {
    OrganizationLocalServiceUtil.getOrganizations(start, end).asScala
  }

  def getOrganizations(companyId: Long): Seq[LOrganization] = {
    OrganizationLocalServiceUtil.getOrganizations(companyId, -1L).asScala
  }


  def getGroupOrganizationsIds(groupId: Long): Seq[Long] =
    OrganizationLocalServiceUtil.getGroupOrganizations(groupId).asScala.map(_.getOrganizationId)

  def addOrganization(userId: Long,
                      parentOrganizationId: Long,
                      name: String,
                      orgType: String,
                      recursable: Boolean,
                      regionId: Long,
                      countryId: Long,
                      statusId: Int,
                      comments: String,
                      site: Boolean,
                      serviceContext: ServiceContext): LOrganization =
    OrganizationLocalServiceUtil.addOrganization(userId, parentOrganizationId, name, orgType, regionId,
      countryId, statusId, comments, site, serviceContext)

  def getCount(organizatinIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String]): Long = {
    val query = dynamicQuery(organizatinIds, contains, companyId, nameLike)
    OrganizationLocalServiceUtil.dynamicQueryCount(query)
  }

  def getOrganizations(organizatinIds: Seq[Long],
                       contains: Boolean,
                       companyId: Long,
                       nameLike: Option[String],
                       ascending: Boolean,
                       startEnd: Option[(Int, Int)]): Seq[Organization] = {
    val order: (String => Order) = if (ascending) OrderFactoryUtil.asc else OrderFactoryUtil.desc

    val query = dynamicQuery(organizatinIds, contains, companyId, nameLike)
      .addOrder(order(NameKey))

    val (start, end) = startEnd.getOrElse((-1, -1))

    OrganizationLocalServiceUtil.dynamicQuery[Organization](query, start, end)
      .asScala
  }


  def dynamicQuery(organizationIds: Seq[Long],
                   contains: Boolean,
                   companyId: Long,
                   nameLike: Option[String]): DynamicQuery = {
    OrganizationLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq("companyId", companyId))
      .addLikeRestriction(NameKey, nameLike)
      .addFilterByValues(IdKey, organizationIds, contains)
  }

  def addGroupOrganizations(courseId: Long, organizationIds: Seq[Long]): Unit = {
    OrganizationLocalServiceUtil.addGroupOrganizations(courseId, organizationIds.toArray)
  }

  def deleteGroupOrganizations(courseId: Long, organizationIds: Seq[Long]): Unit = {
    OrganizationLocalServiceUtil.deleteGroupOrganizations(courseId, organizationIds.toArray)
  }
}
