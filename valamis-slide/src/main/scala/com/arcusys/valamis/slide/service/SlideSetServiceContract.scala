package com.arcusys.valamis.slide.service

import java.io.InputStream
import com.arcusys.valamis.slide.model.SlideSetModel

trait SlideSetServiceContract {
  def getById(id: Long): Option[SlideSetModel]

  def getLogo(slideSetId: Long): Option[Array[Byte]]

  def setLogo(slideSetId: Long, name: String, content: Array[Byte])

  def getSlideSets(titleFilter: String, sortTitleAsc: Boolean, page: Int, itemsOnPage: Int, courseId: Option[Long], isTemplate: Option[Boolean]): List[SlideSetModel]

  def getSlideSetsCount(titleFilter: String, courseId: Option[Long], isTemplate: Option[Boolean]): Int

  def delete(id: Long)

  def clone(id: Long, isTemplate: Boolean, fromTemplate: Boolean, title: String, description: String, logo: Option[String]): SlideSetModel

  def publishSlideSet(id: Long, userId: Long, learnPortletPath: String, courseId: Long): Unit

  def exportSlideSet(id: Long): InputStream

  def importSlideSet(stream: InputStream, scopeId: Int)

  def update(slideSetModel: SlideSetModel): SlideSetModel

  def create(slideSetModel: SlideSetModel): SlideSetModel

  def createWithDefaultSlide(slideSetModel: SlideSetModel): SlideSetModel
}