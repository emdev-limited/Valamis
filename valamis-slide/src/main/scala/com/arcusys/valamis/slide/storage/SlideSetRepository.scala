package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.slide.model.{SlideSetEntity, SlideSetModel}

trait SlideSetRepository {
  def getCount: Int
  def getById(id: Long): Option[SlideSetEntity]
  def getByActivityId(activityId: String): List[SlideSetEntity]
  def getByVersion(activityId: String, version: Double): List[SlideSetEntity]
  def getLastWithCount(
    courseId: Long,
    titleFilter: Option[String],
    orderByAsc: Boolean,
    skipTake: SkipTake): List[(SlideSetEntity, Int)]
  def getTemplatesWithCount(courseId: Long): List[(SlideSetEntity, Int)]
  def getCount(titleFilter: Option[String], courseId: Long): Int
  def delete(id: Long)
  def update(slideSetModel: SlideSetModel): SlideSetEntity
  def updateThemeId(oldThemeId: Long, newThemeId: Option[Long])
  def create(slideSetModel: SlideSetModel): SlideSetEntity
}