package com.arcusys.learn.facades

import com.arcusys.learn.models.CourseResponse
import com.arcusys.valamis.model.{RangeResult, SkipTake}

trait CourseFacadeContract {

  def getCourse(siteId: Long): CourseResponse

  def getByUserId(userId: Long): Seq[CourseResponse]

  def getProgressByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true): RangeResult[CourseResponse]
}
