package com.arcusys.valamis.slide.service

import com.arcusys.valamis.slide.model.SlideThemeModel
import com.arcusys.valamis.slide.storage.{SlideSetRepository, SlideThemeRepositoryContract}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}

class SlideThemeService(implicit val bindingModule: BindingModule) extends Injectable with SlideThemeServiceContract {

  val slideSetRepository = inject[SlideSetRepository]
  val slideThemeRepository = inject[SlideThemeRepositoryContract]

  override def create(model: SlideThemeModel): SlideThemeModel = slideThemeRepository.create(model)

  override def update(model: SlideThemeModel): SlideThemeModel = slideThemeRepository.update(model)

  override def delete(id: Long): Unit = {
    slideSetRepository.updateThemeId(id, None)
    slideThemeRepository.delete(id)
  }

  override def getById(id: Long): SlideThemeModel = slideThemeRepository.get(id)

  override def getAll: Seq[SlideThemeModel] = slideThemeRepository.getAll

  override def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideThemeModel] =
    slideThemeRepository.getBy(userId, isDefault)
}
