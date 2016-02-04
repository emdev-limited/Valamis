package com.arcusys.learn.utils

import com.arcusys.learn.liferay.LiferayClasses.LGroup

object LiferayGroupExtensions {
  implicit class CourseFriendlyUrlExt(val course: LGroup) extends AnyVal {
    def getCourseFriendlyUrl = {
      course.getFriendlyURL match {
        case str: String if !str.isEmpty =>
          if (course.getPublicLayoutsPageCount > 0) s"/web$str"
          else if (course.getPrivateLayoutsPageCount > 0) s"/group$str"
          else ""
        case _ => ""
      }
    }
  }
}
