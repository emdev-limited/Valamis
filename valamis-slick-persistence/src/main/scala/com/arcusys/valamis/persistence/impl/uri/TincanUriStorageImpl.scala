package com.arcusys.valamis.persistence.impl.uri

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.uri.model.TincanURI
import com.arcusys.valamis.uri.model.TincanURIType.TincanURIType
import com.arcusys.valamis.uri.storage.TincanURIStorage

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class TincanUriStorageImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends TincanURIStorage
  with TypeMapper
  with TincanUriTableComponent
  with SlickProfile {

  import driver.simple._

  override def get(uri: String): Option[TincanURI] = {
    db.withSession { implicit session =>
      tincanUris.filter(_.uri === uri).firstOption
    }
  }

  override def delete(uri: String): Unit = {
    db.withSession { implicit session =>
      tincanUris.filter(_.uri === uri).delete
    }
  }

  override def getById(objId: String, objType: TincanURIType): Option[TincanURI] = {
    db.withSession { implicit session =>
      tincanUris.filter(r => r.objectId === objId && r.objectType === objType).firstOption
    }
  }

  override def create(uri: String, objId: String, objType: TincanURIType, content: String): TincanURI = {
    db.withSession { implicit session =>
      val entity = new TincanURI(uri, objId, objType, content)
      tincanUris += entity
      entity
    }
  }

  override def getAll(skipTake: Option[SkipTake], filter: String): Seq[TincanURI] = {
    db.withSession { implicit session =>
      val query = tincanUris
        .filter(_.uri like s"%$filter%")

      skipTake match {
        case Some(SkipTake(skip, take)) => query.drop(skip).take(take).list
        case _ => query.list
      }
    }

  }
}
