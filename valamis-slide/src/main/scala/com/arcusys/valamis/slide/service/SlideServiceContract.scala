package com.arcusys.valamis.slide.service

import com.arcusys.valamis.lrs.util.TinCanVerb
import com.arcusys.valamis.slide.model.{Device, SlideModel}
import java.awt.image.BufferedImage

trait SlideServiceContract {
  def create(slideModel: SlideModel): SlideModel
  def getAll(isTemplate: Option[Boolean]): List[SlideModel]
  def getById(id: Long): Option[SlideModel]
  def getBySlideSetId(slideSetId: Long, isTemplate: Option[Boolean]): List[SlideModel]
  def getLogo(slideId: Long): Option[Array[Byte]]
  def setLogo(slideId: Long, name: String, content: Array[Byte])
  def getTinCanVerbs(): List[TinCanVerb]
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
    fromTemplate: Boolean,
    cloneElements: Boolean): Option[SlideModel]
  def parsePDF(content: Array[Byte], slideId: Long, slideSetId: Long): List[(Long, String)]
  def parsePPTX(content: Array[Byte], slideId: Long, slideSetId: Long, fileName: String): List[(Long, String)]
  def addSlidesToSlideSet(slideId: Long, slideSetId: Long, pages: List[BufferedImage]): List[(Long, String)]
  def copyFileFromTheme(slideId: Long, themeId: Long): Option[SlideModel]
}
