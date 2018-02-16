package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.SlideTheme

trait SlideThemeRepository {
  def get(id: Long): Option[SlideTheme]
  def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideTheme]
  def delete(id: Long)
  def create(theme: SlideTheme): SlideTheme
  def update(theme: SlideTheme): SlideTheme
  def isExist(id: Long): Boolean
  def updateBgImage(id: Long, bgImage: Option[String]): Unit
}
