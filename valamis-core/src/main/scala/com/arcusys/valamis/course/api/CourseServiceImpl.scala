package com.arcusys.valamis.course.api

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services._
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.tag.TagService

abstract class CourseServiceImpl extends CourseService {

  protected lazy val groupService = GroupLocalServiceHelper

  def courseTagService: TagService[LGroup]

  protected def isVisible(gr: LGroup) = gr.isActive

  protected val notPersonalSite = (gr: LGroup) => !(gr.isUserPersonalSite || gr.isUser)

  override def getById(courseId: Long): Option[LGroup] = {
    groupService.fetchGroup(courseId)
  }

  override def getByUserId(userId: Long): Seq[LGroup] = {
    groupService.getGroupsByUserId(userId)
  }

  override def getByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true) = {
    val courses =
      if (skipTake.isDefined)
        groupService.getGroupsByUserId(userId, skipTake.get.skip, skipTake.get.take + skipTake.get.skip, sortAsc)
      else
        groupService.getGroupsByUserId(userId)

    val total = groupService.getGroupsCountByUserId(userId)

    RangeResult(
      total,
      courses
    )
  }

  override def getSitesByUserId(userId: Long): Seq[LGroup] = {
    groupService.getUserSitesGroups(userId)
      .filter(notPersonalSite)
      .filter(isVisible)
  }

  override def getByCompanyId(companyId: Long, skipCheckActive: Boolean = false): Seq[LGroup] = {
    groupService
      .getCompanyGroups(companyId)
      .filter(x => x.isSite && (skipCheckActive || x.isActive) && x.getFriendlyURL != "/control_panel")
  }
}