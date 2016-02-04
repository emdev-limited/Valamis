package com.arcusys.valamis.lrs

import com.arcusys.valamis.lrsEndpoint.model.AuthType.AuthType
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrsEndpoint.storage.LrsEndpointStorage
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class LrsEndpointStorageImpl(val db: JdbcBackend#DatabaseDef,
                             val driver: JdbcProfile)
  extends LrsEndpointStorage
  with LrsEndpointTableComponent {

  import driver.simple._

  implicit val mapper = authTypeMapper

  override def getAll: Seq[LrsEndpoint] =
    db.withSession { implicit s =>
      lrsEndpoint.list
    }

  override def get(auth: AuthType): Option[LrsEndpoint] =
    db.withSession { implicit s =>
      lrsEndpoint.filter(_.authType === auth).firstOption
    }

  override def deleteAll(): Unit =
    db.withSession { implicit s =>
      lrsEndpoint.filter(_.id.?.isDefined).delete //PACL with mySQL crashes here
    }

  override def deleteExternal(): Unit =
    db.withSession { implicit s =>
      lrsEndpoint.filterNot(_.authType === AuthType.INTERNAL).delete
    }

  override def create(entity: LrsEndpoint): LrsEndpoint =
    db.withSession { implicit s =>
      val newId = (lrsEndpoint returning lrsEndpoint.map(_.id)) += entity
      entity.copy(id = Option(newId))
    }
}
