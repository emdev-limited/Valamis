package com.arcusys.valamis.course.model

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import org.joda.time.DateTime


case class CourseInfo(id: Long,
                      name: String,
                      description: String,
                      isActive: Boolean,
                      friendlyUrl: String,
                      groupType: Int,
                      companyId: Long,
                      isUserPersonalSite: Boolean,
                      isUser: Boolean,
                      descriptiveName: String,
                      organizationId: Long,
                      isSite: Boolean,
                      classPK: Long,
                      privateLayoutsPageCount: Int,
                      publicLayoutsPageCount: Int,
                      longDescription: Option[String] = None,
                      userLimit: Option[Int] = None,
                      beginDate: Option[DateTime] = None,
                      endDate: Option[DateTime] = None,
                      userCount: Option[Int] = None
                     )

object CourseInfo {
  def apply(lGroup: LGroup): CourseInfo = new CourseInfo(
    id = lGroup.getGroupId,
    name = lGroup.getName,
    description = lGroup.getDescription,
    isActive = lGroup.isActive,
    friendlyUrl = lGroup.getFriendlyURL,
    groupType = lGroup.getType,
    companyId = lGroup.getCompanyId,
    isUserPersonalSite = lGroup.isUserPersonalSite,
    isUser = lGroup.isUser,
    descriptiveName = lGroup.getDescriptiveName,
    organizationId = lGroup.getOrganizationId,
    isSite = lGroup.isSite,
    classPK = lGroup.getClassPK,
    privateLayoutsPageCount = lGroup.getPrivateLayoutsPageCount,
    publicLayoutsPageCount = lGroup.getPublicLayoutsPageCount,
    longDescription = None,
    userLimit = None,
    beginDate = None,
    endDate = None,
    userCount = None
  )
}