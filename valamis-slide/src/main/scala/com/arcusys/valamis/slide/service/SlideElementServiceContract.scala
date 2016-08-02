package com.arcusys.valamis.slide.service

import com.arcusys.valamis.slide.model.SlideElementModel

trait SlideElementServiceContract {
  def create(slideElementModel: SlideElementModel): SlideElementModel
  def getAll: List[SlideElementModel]
  def getById(id: Long): Option[SlideElementModel]
  def getBySlideId(slideId: Long): List[SlideElementModel]
  def getLogo(slideElementId: Long): Option[Array[Byte]]
  def setLogo(slideElementId: Long, name: String, content: Array[Byte])
  def update(slideElementModel: SlideElementModel): SlideElementModel
  def delete(id: Long)
  def clone(slideElement: SlideElementModel, slideId: Long, isTemplate: Boolean): SlideElementModel
}