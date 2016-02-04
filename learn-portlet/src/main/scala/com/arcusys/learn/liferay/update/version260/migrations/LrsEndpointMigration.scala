package com.arcusys.learn.liferay.update.version260.migrations

import com.arcusys.valamis.core.SlickProfile
import com.arcusys.valamis.lrs.LrsEndpointTableComponent
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.liferay.portal.kernel.log.LogFactoryUtil

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{StaticQuery, GetResult, JdbcBackend}
import scala.util.Try

class LrsEndpointMigration(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends LrsEndpointTableComponent
  with SlickProfile {

  val log = LogFactoryUtil.getLog(getClass)

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
    if (AuthType.isValid(entity.authType.getOrElse("")))
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
