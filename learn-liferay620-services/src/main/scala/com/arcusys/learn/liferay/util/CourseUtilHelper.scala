package com.arcusys.learn.liferay.util

import com.liferay.portal.service.GroupLocalServiceUtil


object CourseUtilHelper {

  def getLink(courseId: Long): String = {
    val course = GroupLocalServiceUtil.getGroup(courseId)

    if (course.getPrivateLayoutsPageCount > 0)
      "/group" + course.getFriendlyURL
    else
      "/web" + course.getFriendlyURL
  }

  def getName(courseId: Long): Option[String] = {
    Option(GroupLocalServiceUtil.fetchGroup(courseId))
      .map(_.getDescriptiveName)
  }

  def getCompanyId(courseId: Long): Long = {
    GroupLocalServiceUtil.getGroup(courseId).getCompanyId
  }
}
