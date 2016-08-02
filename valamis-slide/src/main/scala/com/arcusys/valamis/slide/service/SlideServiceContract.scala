package com.arcusys.valamis.slide.service

import com.arcusys.valamis.lrs.service.util.TinCanVerb
import com.arcusys.valamis.slide.model.SlideModel

trait SlideServiceContract {
  def create(slideModel: SlideModel): SlideModel
  def getAll(isTemplate: Option[Boolean]): List[SlideModel]
  def getById(id: Long): Option[SlideModel]
  def getBySlideSetId(slideSetId: Long, isTemplate: Option[Boolean] = None): List[SlideModel]
  def getLogo(slideId: Long): Option[Array[Byte]]
  def setLogo(slideId: Long, name: String, content: Array[Byte])
  def getTinCanVerbs: List[TinCanVerb]
  def countBySlideSet(slideSetId: Long, isTemplate: Option[Boolean] = None): Long
  def update(slideModel: SlideModel): SlideModel
  def delete(id: Long)
  def clone(
    slideId: Long,
    leftSlideId: Option[Long],
    topSlideId: Option[Long],
    bgImage: Option[String],
    slideSetId: Long,
    isTemplate: Boolean,
    isLessonSummary: Boolean,
    fromTemplate: Boolean): Option[SlideModel]
  def parsePDF(content: Array[Byte]): List[String]
  def parsePPTX(content: Array[Byte], fileName: String): List[String]
  def copyFileFromTheme(slideId: Long, themeId: Long): Option[SlideModel]
}
