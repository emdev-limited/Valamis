package com.arcusys.learn.liferay.util

import com.arcusys.learn.liferay.services.GroupLocalServiceHelper

object CourseUtilHelper {

  def getLink (courseId: Long): String = {
    val course = GroupLocalServiceHelper.getGroup(courseId)

    if(course.getPrivateLayoutsPageCount > 0)
      "/group" + course.getFriendlyURL
    else
      "/web" + course.getFriendlyURL
  }

  def getName (courseId: Long): String = {
    GroupLocalServiceHelper.getGroup(courseId).getDescriptiveName
  }

  def getCompanyId (courseId: Long): Long = {
    GroupLocalServiceHelper.getGroup(courseId).getCompanyId
  }
}
