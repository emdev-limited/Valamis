package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.SlideElement

trait SlideElementRepository {
  def create(element: SlideElement): SlideElement
  def getById(id: Long): Option[SlideElement]
  def getBySlideId(slideId: Long): Seq[SlideElement]
  def update(element: SlideElement): SlideElement
  def delete(id: Long)
  def updateContent(id: Long, content: String): Unit
}