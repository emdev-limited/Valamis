package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateNodeModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ActivityStateNodeTableComponent
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ActivityStateNodeMigration(val db: JdbcBackend#DatabaseDef,
                                 val driver: JdbcProfile)
  extends ActivityStateNodeTableComponent
    with SlickProfile {

  import driver.simple._

  val activityStateMigration = new ActivityStateMigration(db, driver)


  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld


    if (entities.nonEmpty) {
      entities.foreach(a => {
        val newId = activityStateNodeTQ.returning(activityStateNodeTQ.map(_.id)).insert(a)
        activityStateMigration.migrate(true, a.id.get, newId)
      })
    }
  }

  private def getOld(implicit s: JdbcBackend#Session): Seq[ActivityStateNodeModel] = {
    implicit val reader = GetResult[ActivityStateNodeModel](r => ActivityStateNodeModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLongOption(), // INTEGER null,
      r.nextLongOption(), // INTEGER null,
      r.nextStringOption() // TEXT null
    ))

    StaticQuery.queryNA[ActivityStateNodeModel]("select * from Learn_LFActivityStateNode").list
  }
}
