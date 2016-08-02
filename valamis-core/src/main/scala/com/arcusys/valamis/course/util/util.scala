package com.arcusys.valamis.course

import com.arcusys.learn.liferay.LiferayClasses._

package object util {

  implicit class CourseFriendlyUrlExt(val course: LGroup) extends AnyVal {
    def getCourseFriendlyUrl: String = {
      course.getFriendlyURL match {
        case str: String if !str.isEmpty =>
          if (course.getPublicLayoutsPageCount > 0) {
            s"/web$str"
          }
          else if (course.getPrivateLayoutsPageCount > 0) {
            s"/group$str"
          }
          else {
            if (course.getOrganizationId != 0) course.getFriendlyURL else ""
          }
        case _ => ""
      }
    }
  }

}
