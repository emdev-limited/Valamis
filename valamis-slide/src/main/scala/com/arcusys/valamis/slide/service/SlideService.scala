package com.arcusys.valamis.slide.service

import com.arcusys.valamis.slide.model.Slide

trait SlideService {
  def create(slide: Slide): Slide
  def getTemplateSlides: Seq[Slide]
  def getRootSlide(slides: Seq[Slide], isTemplate: Boolean = false): Option[Slide]
  def getRightSlide(slides: Seq[Slide], linkId: Long, isTemplate: Boolean = false): Option[Slide]
  def getBottomSlide(slides: Seq[Slide],linkId: Long, isTemplate: Boolean = false): Option[Slide]
  def getById(id: Long): Option[Slide]
  def getSlides(slideSetId: Long): Seq[Slide]
  def getCount(slideSetId: Long): Long
  def getBgImage(id: Long): Option[Array[Byte]]
  def setBgImage(id: Long, name: String, bgSize: String, content: Array[Byte])
  def deleteBgImage(id: Long): Unit
  def update(slide: Slide): Slide
  def isAvailableLink(id: Long): Boolean
  def updateBgImage(slideId: Long, bgImage: Option[String]): Unit
  def delete(id: Long)
  def clone(slide: Slide,
            leftSlideId: Option[Long],
            topSlideId: Option[Long],
            slideSetId: Long,
            isTemplate: Boolean,
            fromTemplate: Boolean): Slide
  def parsePDF(content: Array[Byte]): List[String]
  def parsePPTX(content: Array[Byte], fileName: String): List[String]
}
