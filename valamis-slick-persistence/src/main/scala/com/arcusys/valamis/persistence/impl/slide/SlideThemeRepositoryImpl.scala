package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model.SlideTheme
import com.arcusys.valamis.slide.storage.SlideThemeRepository
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

class SlideThemeRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideThemeRepository
    with SlickProfile
    with DatabaseLayer
    with SlideTableComponent {

  import driver.api._

  override def create(theme: SlideTheme): SlideTheme = execSync {
    (slideThemes returning slideThemes.map(_.id)).into { (row, newId) =>
      row.copy(id = newId)
    } += theme
  }

  override def update(theme: SlideTheme): SlideTheme = {
    val action = slideThemes.filter(_.id === theme.id)
      .map(t => (
        t.title,
        t.bgColor,
        t.font,
        t.questionFont,
        t.answerFont,
        t.answerBg,
        t.userId,
        t.isDefault))
      .update(theme.title,
        theme.bgColor,
        theme.font,
        theme.questionFont,
        theme.answerFont,
        theme.answerBg,
        theme.userId,
        theme.isDefault)
    execSync(action)
    theme
  }

  override def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideTheme] = execSync {
    slideThemes
      .filter{theme => (((theme.userId.isEmpty && userId.isEmpty) || theme.userId === userId)
        && theme.isDefault === isDefault)}.result
  }

  override def get(id: Long): Option[SlideTheme] = execSync {
    slideThemes.filter(_.id === id).result.headOption
  }

  override def delete(id: Long): Unit = execSync {
    val updateSlideSet = slideSets
      .filter(_.themeId === id)
      .map(_.themeId)
      .update(None)

    val deleteTheme = slideThemes.filter(_.id === id).delete
    updateSlideSet >> deleteTheme
  }

  override def isExist(id: Long): Boolean =  execSync {
    slideThemes.filter(_.id === id).exists.result
  }

  override def updateBgImage(id: Long, bgImage: Option[String]): Unit = execSync {
    slideThemes.filter(_.id === id)
      .map(_.bgImage)
      .update(bgImage)
  }

}
