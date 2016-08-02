package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityDataMapModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ActivityDataMapTableComponent
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ActivityDataMapMigration(val db: JdbcBackend#DatabaseDef,
                               val driver: JdbcProfile)
  extends ActivityDataMapTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate()(implicit s: JdbcBackend#Session): Unit = {
    val entities = getOld

    if (entities.nonEmpty) {
      activityDataMapTQ ++= entities
    }
  }

  private def getOld(implicit s: JdbcBackend#Session): Seq[ActivityDataMapModel] = {
    implicit val reader = GetResult[ActivityDataMapModel](r => ActivityDataMapModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLongOption(), // INTEGER null,
      r.nextStringOption(), // VARCHAR(512) null,
      r.nextString(), // TEXT null,
      r.nextBoolean(), // BOOLEAN null,
      r.nextBoolean() // BOOLEAN null
    ))

    StaticQuery.queryNA[ActivityDataMapModel]("select * from Learn_LFActivityDataMap").list
  }
}
