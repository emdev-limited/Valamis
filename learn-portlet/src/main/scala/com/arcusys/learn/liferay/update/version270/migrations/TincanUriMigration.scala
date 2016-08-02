package com.arcusys.learn.liferay.update.version270.migrations

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.valamis.persistence.impl.uri.TincanUriTableComponent
import com.arcusys.valamis.uri.model.{TincanURI, TincanURIType}
import com.arcusys.valamis.util.enumeration._
import slick.jdbc.GetResult

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class TincanUriMigration(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends TincanUriTableComponent
  with SlickProfile {

  val log = LogFactoryHelper.getLog(getClass)

  import driver.simple._

  case class OldEntity(uri: String,
                       objId: Option[String],
                       objType: Option[String],
                       content: Option[String])

  implicit val getStatement = GetResult[OldEntity] { r => OldEntity(
    r.nextString(),
    r.nextStringOption(),
    r.nextStringOption(),
    r.nextStringOption()
  )
  }

  def getOldData(implicit session: JdbcBackend#Session) = {
    StaticQuery.queryNA[OldEntity](s"SELECT * FROM Learn_LFTincanURI").list
      .filter(row => row.objId.isDefined && row.objType.isDefined && row.content.isDefined)
  }

  def toNewData(entity: OldEntity): Option[TincanURI] = {
    if (entity.objType.exists(TincanURIType.isValid))
      Some(TincanURI(
        entity.uri,
        entity.objId.get,
        TincanURIType.withName(entity.objType.get),
        entity.content.get
      ))
    else {
      log.warn(s"The value of objType ${entity.objType} is incorrect")
      None
    }
  }

  def migrate(): Unit = {
    db.withTransaction { implicit session =>
      tincanUris.ddl.create
      val newRows = getOldData flatMap toNewData
      if (newRows.nonEmpty) tincanUris ++= newRows
    }
  }

}
