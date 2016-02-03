package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.SlidePropertyEntity

trait SlidePropertyRepository {
  def getBySlideId(slideId: Long): Seq[SlidePropertyEntity]
  def create(property: SlidePropertyEntity)
  def delete(slideId: Long)
}
