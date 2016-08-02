package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.{SlideEntity, SlideModel}

trait SlideRepository {
  def getCount: Int
  def getAll(isTemplate: Option[Boolean]): List[SlideEntity]
  def getById(id: Long): Option[SlideEntity]
  def getBySlideSetId(slideSetId: Long, isTemplate: Option[Boolean]): List[SlideEntity]
  def countBySlideSetId(slideSetId: Long, isTemplate: Option[Boolean]): Long
  def delete(id: Long)
  def create(slideModel: SlideModel): SlideEntity
  def update(slideModel: SlideModel): SlideEntity
}
