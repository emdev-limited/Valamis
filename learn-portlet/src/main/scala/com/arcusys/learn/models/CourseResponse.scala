package com.arcusys.learn.models

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.learn.utils.LiferayGroupExtensions._

/**
 * Created by Iliya Tryapitsin on 12.03.14.
 */
case class CourseResponse(id: Long,
  title: String,
  url: String,
  description: String,

  users: Option[Int] = None,
  completed: Option[Int] = None)


trait CourseConverter {
  protected def toResponse(lGroup: LGroup): CourseResponse =
    CourseResponse(
      lGroup.getGroupId,
      lGroup.getDescriptiveName,
      lGroup.getCourseFriendlyUrl,
      lGroup.getDescription.replace("\n", " "))
}