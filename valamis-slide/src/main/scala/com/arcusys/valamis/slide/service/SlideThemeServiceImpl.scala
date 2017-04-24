package com.arcusys.valamis.slide.service

import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.slide.model.SlideTheme
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.slide.storage.{SlideSetRepository, SlideThemeRepository}

abstract class SlideThemeServiceImpl extends SlideThemeService {

  def slideSetRepository: SlideSetRepository
  def slideThemeRepository: SlideThemeRepository
  def fileService: FileService

  override def create(model: SlideTheme): SlideTheme = slideThemeRepository.create(model)

  override def update(model: SlideTheme): SlideTheme = slideThemeRepository.update(model)

  override def delete(id: Long): Unit = slideThemeRepository.delete(id)

  override def getById(id: Long): SlideTheme =
    slideThemeRepository.get(id)
      .getOrElse(throw new EntityNotFoundException(s"Theme with id $id not found"))

  override def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideTheme] =
    slideThemeRepository.getBy(userId, isDefault)

  override def setBgImage(id: Long, name: String, bgSize: String, content: Array[Byte]): Unit = {
    slideThemeRepository.get(id).foreach { theme =>
      fileService.setFileContent(
        folder = filePathPrefix(theme),
        name = name,
        content = content,
        deleteFolder = false
      )
      slideThemeRepository.updateBgImage(id, Some(name + ' ' + bgSize))
    }
  }
}
