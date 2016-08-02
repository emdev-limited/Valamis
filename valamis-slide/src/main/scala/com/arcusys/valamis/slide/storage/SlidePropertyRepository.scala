package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.{SlideModel, SlidePropertyEntity}

trait SlidePropertyRepository {
  def getBySlideId(slideId: Long): Seq[SlidePropertyEntity]
  def create(slideModel: SlideModel, newSlideId: Long)
  def delete(slideId: Long)
  def replace(slideModel: SlideModel, newSlideId: Long)
}
