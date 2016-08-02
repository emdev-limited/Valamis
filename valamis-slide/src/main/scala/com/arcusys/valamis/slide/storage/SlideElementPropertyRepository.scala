package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.{SlideElementModel, SlideElementPropertyEntity}

/**
 * Created by Igor Borisov on 02.11.15.
 */
trait SlideElementPropertyRepository {
  def getBySlideElementId(slideElementId: Long): Seq[SlideElementPropertyEntity]
  def create(slideElement: SlideElementModel, newSlideSetId: Long)
  def delete(slideElementId: Long)
  def replace(slideElement: SlideElementModel, newSlideSetId: Long)
  def createFromOldValues(deviceId: Long,
                          slideElementId: Long,
                          top: String,
                          left: String,
                          width: String,
                          height: String): Unit
}
