package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.{SlideElementModel, SlideElementPropertyEntity}

/**
 * Created by Igor Borisov on 02.11.15.
 */
trait SlideElementPropertyRepository {
  def getBySlideElementId(slideElementId: Long): Seq[SlideElementPropertyEntity]
  def create(property: SlideElementPropertyEntity)
  def delete(slideElementId: Long)
  def createFromOldValues(deviceId: Long, slideElementId: Long, slideElement: SlideElementModel): Unit
}
