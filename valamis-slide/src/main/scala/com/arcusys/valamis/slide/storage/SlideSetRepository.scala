package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.{SlideSetModel, SlideSetEntity}

trait SlideSetRepository {
  def getCount: Int
  def getById(id: Long): Option[SlideSetEntity]
  def getSlideSets(titleFilter: String, sortTitleAsc: Boolean, page: Int, itemsOnPage: Int, courseId: Option[Long], isTemplate: Option[Boolean]): List[SlideSetEntity]
  def getSlideSetsCount(titleFilter: String, courseId: Option[Long], isTemplate: Option[Boolean]): Int
  def delete(id: Long)
  def update(slideSetModel: SlideSetModel): SlideSetEntity
  def updateThemeId(oldThemeId: Long, newThemeId: Option[Long])
  def create(slideSetModel: SlideSetModel): SlideSetEntity
}