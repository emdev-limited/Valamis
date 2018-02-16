package com.arcusys.valamis.slide.service

import com.arcusys.valamis.slide.model.SlideElement

trait SlideElementService {
  def getById(id: Long): Option[SlideElement]
  def getBySlideId(slideId: Long): Seq[SlideElement]
  def getLogo(id: Long): Option[Array[Byte]]
  def setLogo(id: Long, name: String, content: Array[Byte])
  def create(element: SlideElement): SlideElement
  def update(element: SlideElement): SlideElement
  def updateContent(id: Long, content: String): Unit
  def delete(id: Long)
  def clone(element: SlideElement, isTemplate: Boolean): SlideElement
}