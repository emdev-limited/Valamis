package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.LOrganization
import com.arcusys.learn.liferay.services.dynamicQuery._
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.model.Organization
import com.liferay.portal.service.OrganizationLocalServiceUtil
import scala.collection.JavaConverters.asScalaBufferConverter

object OrganizationLocalServiceHelper {
  val NameKey = "name"
  val IdKey = "organizationId"
  val CompanyIdKey = "companyId"

  def getOrganizations(start: Int = QueryUtil.ALL_POS,
                       end: Int = QueryUtil.ALL_POS): Seq[LOrganization] = {
    OrganizationLocalServiceUtil.getOrganizations(CompanyHelper.getCompanyId, -1, start, end).asScala
  }

  def getOrganizations(companyId: Long): Seq[LOrganization] = {
    OrganizationLocalServiceUtil.getOrganizations(companyId, -1L).asScala
  }

  def getGroupOrganizationsIds(groupId: Long): Seq[Long] =
    OrganizationLocalServiceUtil.getGroupOrganizations(groupId).asScala.map(_.getOrganizationId)

  def getCount(organizationIds: Seq[Long],
               contains: Boolean,
               companyId: Long,
               nameLike: Option[String]): Long = {
    val query = dynamicQuery(organizationIds, contains, companyId, nameLike)
    OrganizationLocalServiceUtil.dynamicQueryCount(query)
  }

  def getOrganizations(organizationIds: Seq[Long],
                       contains: Boolean,
                       companyId: Long,
                       nameLike: Option[String],
                       ascending: Boolean,
                       startEnd: Option[(Int, Int)]): Seq[Organization] = {
    val order: (String => Order) = if (ascending) OrderFactoryUtil.asc else OrderFactoryUtil.desc

    val query = dynamicQuery(organizationIds, contains, companyId, nameLike)
      .addOrder(order(NameKey))

    val (start, end) = startEnd.getOrElse((-1, -1))

    OrganizationLocalServiceUtil.dynamicQuery(query, start, end)
      .asScala.map(_.asInstanceOf[Organization])
  }

  private def dynamicQuery(organizationIds: Seq[Long],
                           contains: Boolean,
                           companyId: Long,
                           nameLike: Option[String]): DynamicQuery = {
    OrganizationLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq(CompanyIdKey, companyId))
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
