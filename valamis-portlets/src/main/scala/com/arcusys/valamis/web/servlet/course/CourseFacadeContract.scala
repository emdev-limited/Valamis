package com.arcusys.valamis.web.servlet.course

import com.arcusys.valamis.model.{RangeResult, SkipTake}

trait CourseFacadeContract {

  def getCourse(siteId: Long): Option[CourseResponse]

  def getByUserId(userId: Long): Seq[CourseResponse]

  def getProgressByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true): RangeResult[CourseResponse]
}
