package com.arcusys.valamis.slide.service

import com.arcusys.valamis.slide.model.SlideTheme

trait SlideThemeService {
  def create(theme: SlideTheme): SlideTheme
  def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideTheme]
  def getById(id: Long): SlideTheme
  def update(theme: SlideTheme): SlideTheme
  def delete(id: Long)
  def setBgImage(id: Long, name: String, bgSize: String, content: Array[Byte]): Unit
}
