package com.arcusys.valamis.persistence.impl.settings

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.settings.model.StatementToActivity
import com.arcusys.valamis.settings.storage.StatementToActivityStorage

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class StatementToActivityStorageImpl(val db: JdbcBackend#DatabaseDef,
                                     val driver: JdbcProfile)
  extends StatementToActivityStorage
    with SlickProfile
  with StatementToActivityTableComponent {

  import driver.simple._

  override def getAll: Seq[StatementToActivity] = {
    db.withSession { implicit s =>
      statementToActivity.list
    }
  }

  def getById(id: Long): Option[StatementToActivity] = {
    db.withSession { implicit s =>
      statementToActivity.filter(_.id === id).firstOption
    }
  }

  def getByCourseId(courseId: Long): Seq[StatementToActivity] = {
    db.withSession { implicit s =>
      statementToActivity.filter(_.courseId === courseId).list
    }
  }

  def create(entity: StatementToActivity): StatementToActivity = {
    db.withSession { implicit s =>
      (statementToActivity returning statementToActivity) += entity
    }
  }

  def modify(entity: StatementToActivity) = db.withSession { implicit s =>
    statementToActivity.filter(_.id === entity.id).map(_.update).update(entity)
  }

  def delete(id: Long) = db.withSession { implicit s =>
    statementToActivity.filter(_.id === id).delete
  }
}
