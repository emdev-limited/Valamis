package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.AttemptDataModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptDataTableComponent, AttemptTableComponent, ScormUserComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class AttemptDataMigration(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends AttemptDataTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  import driver.simple._

  def migrate(attemptOldId: Long, attemptNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOldRows(attemptOldId)


    if (entities.nonEmpty) {
      attemptDataTQ ++= entities.map(_.copy(attemptId = attemptNewId))
    }

  }

  private def getOldRows(attemptId: Long)(implicit s: JdbcBackend#Session): Seq[AttemptDataModel] = {
    implicit val reader = GetResult[AttemptDataModel](r => AttemptDataModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextString(), // VARCHAR(3000) null,
      r.nextStringOption(), // TEXT null,
      r.nextLong(), // INTEGER null,
      r.nextString() // VARCHAR(3000) null
    ))

    StaticQuery.queryNA[AttemptDataModel](s"select * from Learn_LFAttemptData where attemptID = $attemptId").list
  }
}
