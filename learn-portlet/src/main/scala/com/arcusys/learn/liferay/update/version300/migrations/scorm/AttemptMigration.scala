package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.AttemptModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptTableComponent, ScormUserComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class AttemptMigration(val db: JdbcBackend#DatabaseDef,
                       val driver: JdbcProfile)
  extends AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  import driver.simple._

  val attemptDataMigration = new AttemptDataMigration(db, driver)
  val activityStateTreeMigration = new ActivityStateTreeMigration(db, driver)


  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val attempts = getOldAttempts


    if (attempts.nonEmpty) {
      attempts.foreach(a => {
        val newId = attemptTQ.returning(attemptTQ.map(_.id)).insert(a)
        attemptDataMigration.migrate(a.id.get, newId)
        activityStateTreeMigration.migrate(a.id.get, newId)
      })
    }

  }

  private def getOldAttempts(implicit s: JdbcBackend#Session): Seq[AttemptModel] = {
    implicit val reader = GetResult[AttemptModel](r => AttemptModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // INTEGER null,
      r.nextLong(), // INTEGER null,
      r.nextString(), // TEXT null,
      r.nextBoolean() // BOOLEAN null
    ))

    StaticQuery.queryNA[AttemptModel]("select * from Learn_LFAttempt").list
  }
}
