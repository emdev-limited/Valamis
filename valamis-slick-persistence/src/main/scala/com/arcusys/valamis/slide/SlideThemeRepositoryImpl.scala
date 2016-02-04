package com.arcusys.valamis.slide

import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.slide.model.SlideThemeModel
import com.arcusys.valamis.slide.storage.SlideThemeRepositoryContract

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SlideThemeRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideThemeRepositoryContract
  with SlideTableComponent {

  import driver.simple._

  override def create(model: SlideThemeModel): SlideThemeModel = db.withSession { implicit s =>
    val id = (slideThemes returning slideThemes.map(_.id)).insert(model)
    slideThemes.filter(_.id === id).first
  }

  override def update(model: SlideThemeModel): SlideThemeModel = db.withSession { implicit s =>
    val changedRows = slideThemes.filter(_.id === model.id.get).update(model)
    if (changedRows == 0) throw new EntityNotFoundException(s"Theme with id ${model.id} not found")
    model
  }

  override def getAll: Seq[SlideThemeModel] =  db.withSession { implicit s =>
    slideThemes.list
  }

  override def getBy(userId: Option[Long], isDefault: Boolean): Seq[SlideThemeModel] =  db.withSession { implicit s =>
    slideThemes
      .list
      .filter{ theme =>
      (((theme.userId.isEmpty && userId.isEmpty) || theme.userId == userId)
      && theme.isDefault == isDefault)}
  }

  override def get(id: Long): SlideThemeModel = db.withSession { implicit s =>
    val model = slideThemes.filter(_.id === id).firstOption
    model.getOrElse(throw new EntityNotFoundException(s"Theme with id ${id} not found"))
  }

  override def delete(id: Long): Unit = db.withTransaction { implicit s =>
    slideThemes.filter(_.id === id).delete
  }

  override def isExist(id: Long): Boolean = db.withTransaction { implicit s =>
    slideThemes.filter(_.id === id).exists.run
  }
}
