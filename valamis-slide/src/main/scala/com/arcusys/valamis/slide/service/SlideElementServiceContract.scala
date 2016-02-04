package com.arcusys.valamis.slide.service

import com.arcusys.valamis.slide.model.{SlideElementsProperties, SlideElementModel}

trait SlideElementServiceContract {
  def create(slideElementModel: SlideElementModel): SlideElementModel
  def getAll: List[SlideElementModel]
  def getById(id: Long): Option[SlideElementModel]
  def getBySlideId(slideId: Long): List[SlideElementModel]
  def getLogo(slideElementId: Long): Option[Array[Byte]]
  def setLogo(slideElementId: Long, name: String, content: Array[Byte])
  def update(slideElementModel: SlideElementModel): SlideElementModel
  def delete(id: Long)
  def clone(
    id: Long,
    slideId: Long,
    correctLinkedSlideId: Option[Long],
    incorrectLinkedSlideId: Option[Long],
    width: String,
    height: String,
    content: String,
    isTemplate: Boolean,
    properties: Seq[SlideElementsProperties]): SlideElementModel
}