package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.SlideThemeModel

trait SlideThemeRepositoryContract {
  def getAll: Seq[SlideThemeModel]
  def get(id: Long): SlideThemeModel
  def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideThemeModel]
  def delete(id: Long)
  def create(model: SlideThemeModel): SlideThemeModel
  def update(model: SlideThemeModel): SlideThemeModel
  def isExist(id: Long): Boolean
}
