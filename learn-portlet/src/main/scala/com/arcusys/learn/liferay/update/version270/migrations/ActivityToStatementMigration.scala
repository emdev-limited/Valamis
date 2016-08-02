package com.arcusys.learn.liferay.update.version270.migrations

import com.arcusys.learn.liferay.services.ClassNameLocalServiceHelper
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.settings.ActivityToStatementTableComponent
import com.arcusys.valamis.settings.model.ActivityToStatement
import slick.jdbc.GetResult

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ActivityToStatementMigration(val db: JdbcBackend#DatabaseDef,
                                   val driver: JdbcProfile)
  extends ActivityToStatementTableComponent
    with SlickProfile {

  import driver.simple._

  case class OldEntity(id: Int,
                       siteId: Option[Int],
                       dataKey: Option[String],
                       dataValue: Option[String])

  implicit val getStatement = GetResult[OldEntity] { r => OldEntity(
    r.nextInt(),
    r.nextIntOption(),
    r.nextStringOption(),
    r.nextStringOption()
  )
  }

  def getOldData(implicit session: JdbcBackend#Session) = {
    StaticQuery.queryNA[OldEntity](s"SELECT * FROM Learn_LFSiteDependentConfig").list
      .filter(row => row.siteId.isDefined && row.dataKey.isDefined && row.dataValue.isDefined)
  }

  def getClassNameId(className: String): Long = {
    ClassNameLocalServiceHelper.getClassNameId(className)
  }

  def toNewData(entity: OldEntity): ActivityToStatement = {
    val classNameId = getClassNameId(entity.dataKey.get)
    ActivityToStatement(
      entity.siteId.get,
      classNameId,
      entity.dataValue.get
    )
  }

  def migrate(): Unit = {
    db.withTransaction { implicit session =>
      activityToStatement.ddl.create
      val newRows = getOldData map toNewData
      if (newRows.nonEmpty) activityToStatement ++= newRows
    }
  }

}
