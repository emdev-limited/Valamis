package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.{SlideElementModel, SlideElementEntity}

trait SlideElementRepository {
  def getCount: Int
  def create(slideElementModel: SlideElementModel): SlideElementEntity
  def getAll: List[SlideElementEntity]
  def getById(id: Long): Option[SlideElementEntity]
  def getBySlideId(id: Long): List[SlideElementEntity]
  def update(slideElementModel: SlideElementModel): SlideElementEntity
  def delete(id: Long)
}