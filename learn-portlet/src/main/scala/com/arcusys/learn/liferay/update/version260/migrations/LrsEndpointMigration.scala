package com.arcusys.learn.liferay.update.version260.migrations

import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.valamis.persistence.impl.lrs.LrsEndpointTableComponent
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.util.enumeration._
import slick.jdbc.GetResult

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class LrsEndpointMigration(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends LrsEndpointTableComponent
  with SlickProfile {

  val log = LogFactoryHelper.getLog(getClass)

  import driver.simple._

  case class OldEntity(id: Long,
                       endpoint: Option[String],
                       authType: Option[String],
                       key: Option[String],
                       secret: Option[String],
                       customHost: Option[String])

  implicit val getStatement = GetResult[OldEntity] { r => OldEntity(
    r.nextLong(),
    r.nextStringOption(),
    r.nextStringOption(),
    r.nextStringOption(),
    r.nextStringOption(),
    r.nextStringOption()
  )
  }

  def getOldData(implicit session: JdbcBackend#Session) = {
    StaticQuery.queryNA[OldEntity](s"SELECT * FROM learn_lftincanlrsendpoint").list
      .filter(row => row.endpoint.isDefined && row.authType.isDefined && row.key.isDefined && row.secret.isDefined)
  }

  def toNewData(entity: OldEntity): Option[LrsEndpoint] = {
    if (entity.authType.exists(AuthType.isValid))
      Some(LrsEndpoint(
        entity.endpoint.get,
        AuthType.withName(entity.authType.get),
        entity.key.get,
        entity.secret.get,
        entity.customHost.filterNot(_ == "")
      ))
    else {
      log.warn(s"The value of authType ${entity.authType} is incorrect")
      None
    }
  }

  def migrate(): Unit = {
    db.withTransaction { implicit session =>
      val newRows = getOldData flatMap toNewData
      if (newRows.nonEmpty) lrsEndpoint ++= newRows
    }
  }
}
