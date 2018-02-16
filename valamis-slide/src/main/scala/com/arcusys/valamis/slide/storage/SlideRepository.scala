package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.{SlideElementPropertyEntity, SlideElement, SlidePropertyEntity, Slide}

trait SlideRepository {
  type SlidesData = (Seq[Slide], Seq[SlidePropertyEntity], Seq[SlideElement], Seq[SlideElementPropertyEntity])

  def getById(id: Long): Option[Slide]
  def getByLinkSlideId(id: Long): Seq[Slide]
  def getBySlideSetId(slideSetId: Long): Seq[Slide]
  def getCountBySlideSetId(slideSetId: Long): Int
  def getSlidesWithData(slideSetId: Long, isTemplate: Boolean): SlidesData
  def delete(id: Long)
  def create(slide: Slide): Slide
  def update(slide: Slide): Slide
  def updateBgImage(id: Long, bgImage: Option[String]): Unit
}
