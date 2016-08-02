package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.services.dynamicQuery._
import com.liferay.portal.kernel.dao.orm._
import com.liferay.portal.model.Organization
import com.liferay.portal.service.OrganizationLocalServiceUtil

import scala.collection.JavaConverters.asScalaBufferConverter

object OrganizationLocalServiceHelper {
  val NameKey = "name"
  val IdKey = "organizationId"
  val CompanyIdKey = "companyId"

  def getOrganizations(start: Int, end: Int): java.util.List[Organization] =
    OrganizationLocalServiceUtil.getOrganizations(start, end)

  def getGroupOrganizations(groupId: Long) = OrganizationLocalServiceUtil.getGroupOrganizations(groupId)

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

    OrganizationLocalServiceUtil.dynamicQuery(query, start, end)
      .asScala.map(_.asInstanceOf[Organization])
  }

  def getOrganizationsIds(namePart: String,
                          companyId: Long
                         ): Seq[Long] = {

    val query = OrganizationLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq(CompanyIdKey, companyId))
      .add(RestrictionsFactoryUtil.ilike(NameKey, namePart))
      .setProjection(ProjectionFactoryUtil.property(IdKey))

    OrganizationLocalServiceUtil.dynamicQuery(query).asInstanceOf[List[Long]]
  }

  private def dynamicQuery(organizationIds: Seq[Long],
                           contains: Boolean,
                           companyId: Long,
                           nameLike: Option[String]): DynamicQuery = {
    OrganizationLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq(CompanyIdKey, companyId))
      .addLikeRestriction(NameKey, nameLike)
      .addInSetRestriction(IdKey, organizationIds, contains)
  }

  def addGroupOrganizations(courseId: Long, organizationIds: Seq[Long]): Unit = {
    OrganizationLocalServiceUtil.addGroupOrganizations(courseId, organizationIds.toArray)
  }

  def deleteGroupOrganizations(courseId: Long, organizationIds: Seq[Long]): Unit = {
    OrganizationLocalServiceUtil.deleteGroupOrganizations(courseId, organizationIds.toArray)
  }
}
