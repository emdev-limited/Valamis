package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ActivityTableComponent, AttemptTableComponent, ScormUserComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ActivityMigration(val db: JdbcBackend#DatabaseDef,
                        val driver: JdbcProfile)
  extends ActivityTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  import driver.simple._

  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val activities = getOld

    if (activities.nonEmpty) {
      activityTQ ++= activities
    }
  }

  private def getOld(implicit s: JdbcBackend#Session): Seq[ActivityModel] = {
    implicit val reader = GetResult[ActivityModel](r => ActivityModel(
      r.nextLong(), //  LONG not null primary key,
      r.nextStringOption(), //  VARCHAR(512) null,
      r.nextLongOption(), //  INTEGER null,
      r.nextString(), //  VARCHAR(512) null,
      r.nextStringOption(), //  VARCHAR(512) null,
      r.nextString(), //  TEXT null,
      r.nextStringOption(), //  TEXT null,
      r.nextStringOption(), //  TEXT null,
      r.nextStringOption(), //  TEXT null,
      r.nextBoolean(), //  BOOLEAN null,
      r.nextBooleanOption(), //  BOOLEAN null,
      r.nextBooleanOption(), //  BOOLEAN null,
      r.nextStringOption(), //  TEXT null,
      r.nextStringOption() //  TEXT null
    ))

    StaticQuery.queryNA[ActivityModel]("select * from Learn_LFActivity").list
  }
}
