package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ScormUserModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ScormUserComponent
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ScormUserMigration(val db: JdbcBackend#DatabaseDef,
                         val driver: JdbcProfile)
  extends ScormUserComponent
    with SlickProfile {

  import driver.simple._

  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val users = getOld


    if (users.nonEmpty) {
      scormUsersTQ ++= users.distinct
    }

  }

  private def getOld(implicit s: JdbcBackend#Session): Seq[ScormUserModel] = {
    implicit val reader = GetResult[ScormUserModel](r => {
      r.nextLong()
      ScormUserModel(
        r.nextLong(), // id_ INTEGER null,
        r.nextString(), // name TEXT null,
        r.nextDoubleOption(), // preferredAudioLevel DOUBLE null,
        r.nextStringOption(), // preferredLanguage TEXT null,
        r.nextDoubleOption(), // preferredDeliverySpeed DOUBLE null,
        r.nextIntOption() // preferredAudioCaptioning INTEGER null
      )
    })

    StaticQuery.queryNA[ScormUserModel]("select * from Learn_LFUser").list
  }
}
