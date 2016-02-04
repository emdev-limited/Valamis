package com.arcusys.valamis.slide.service

import com.arcusys.valamis.slide.model.SlideThemeModel

trait SlideThemeServiceContract {
  def create(model: SlideThemeModel): SlideThemeModel
  def getAll: Seq[SlideThemeModel]
  def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideThemeModel]
  def getById(id: Long): SlideThemeModel
  def update(model: SlideThemeModel): SlideThemeModel
  def delete(id: Long)
}
