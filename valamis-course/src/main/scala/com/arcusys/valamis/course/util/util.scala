package com.arcusys.valamis.course

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.valamis.course.model.CourseInfo

package util {

  trait UrlFormatter {
    def formatFriendlyUrl(friendlyUrl: String, publicPageCount: Int, privatePageCount: Int, organizationId: Long): String = {
      friendlyUrl match {
        case str: String if !str.isEmpty =>
          if (publicPageCount > 0) {
            s"/web$str"
          }
          else if (privatePageCount > 0) {
            s"/group$str"
          }
          else {
            if (organizationId != 0) friendlyUrl else ""
          }
        case _ => ""
      }
    }
  }
}

package object util {

  implicit class CourseFriendlyUrlExt(val course: LGroup) extends UrlFormatter{
    def getCourseFriendlyUrl: String = {
      formatFriendlyUrl(
        course.getFriendlyURL,
        course.getPublicLayoutsPageCount,
        course.getPrivateLayoutsPageCount,
        course.getOrganizationId
      )
    }
  }

  implicit class CourseInfoFriendlyUrlExt(val course: CourseInfo) extends UrlFormatter {
    def getCourseFriendlyUrl: String = {
      formatFriendlyUrl(
        course.friendlyUrl,
        course.publicLayoutsPageCount,
        course.privateLayoutsPageCount,
        course.organizationId
      )
    }
  }
}