package com.arcusys.valamis.course

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.arcusys.learn.liferay.services.GroupLocalServiceHelper
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.liferay.portal.service.CompanyLocalServiceUtil

import scala.collection.JavaConverters._

trait CourseService {
  def getAll: Seq[LGroup]

  def getAll(companyId: Long, skipTake: Option[SkipTake], namePattern: String, sortAscDirection: Boolean): RangeResult[LGroup]
  
  def getByCompanyId(companyId: Long): Seq[LGroup]

  def getById(courseId: Long): Option[LGroup]

  def getGroupIdsForAllCoursesFromAllCompanies: Seq[Long]

  def getByUserId(userId: Long): Seq[LGroup]

  def getByUserId(userId: Long,
                  skipTake: Option[SkipTake],
                  sortAsc: Boolean = true): RangeResult[LGroup]

  def getCompanyIds: Seq[Long]
}

class CourseServiceImpl(implicit val bindingModule: BindingModule)
  extends CourseService
  with Injectable  {

  private lazy val groupService = GroupLocalServiceHelper

  def getAll: Seq[LGroup] = {
    groupService.getGroups.asScala
  }

  def getAll(companyId: Long, skipTake: Option[SkipTake], namePattern: String, sortAscDirection: Boolean): RangeResult[LGroup] = {
    var courses = getByCompanyId(companyId)


   if (!namePattern.isEmpty)
     courses = courses.filter(_.getDescriptiveName.toLowerCase.contains(namePattern.toLowerCase))

    val total = courses.length

    if (!sortAscDirection) courses = courses.reverse

    for(SkipTake(skip, take) <- skipTake)
      courses = courses.slice(skip, skip + take)

    RangeResult(total, courses)
  }
  
  def getByCompanyId(companyId: Long): Seq[LGroup] = {
    groupService
      .getCompanyGroups(companyId, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .asScala
      .filter(x => x.isSite && x.isActive && x.getFriendlyURL != "/control_panel")
  }

  def getById(courseId: Long) = {
    Option(groupService.fetchGroup(courseId))
  }

  def getGroupIdsForAllCoursesFromAllCompanies: Seq[Long] = {
    groupService.getGroupIdsForAllActiveSites
  }

  def getByUserId(userId: Long) = {
    groupService.getGroupsByUserId(userId).asScala
  }

  def getByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true) = {
    val courses =
      if(skipTake.isDefined)
        groupService.getGroupsByUserId(userId, skipTake.get.skip, skipTake.get.take + skipTake.get.skip, sortAsc).asScala
      else
        groupService.getGroupsByUserId(userId).asScala

    val total = groupService.getGroupsCountByUserId(userId)

    RangeResult (
        total,
        courses
      )
  }

  def getCompanyIds: Seq[Long] = CompanyLocalServiceUtil.getCompanies(-1, -1).asScala.map(_.getCompanyId).toSeq
}