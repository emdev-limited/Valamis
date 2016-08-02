package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateTreeModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ActivityStateTreeTableComponent, AttemptTableComponent, ScormUserComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ActivityStateTreeMigration(val db: JdbcBackend#DatabaseDef,
                                 val driver: JdbcProfile)
  extends ActivityStateTreeTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  import driver.simple._

  val activityStateMigration = new ActivityStateMigration(db, driver)

  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld()


    if (entities.nonEmpty) {
      entities.foreach(a => {
        val newId = activityStateTreeTQ.returning(activityStateTreeTQ.map(_.id)).insert(a)
        activityStateMigration.migrate(false, a.id.get, newId)
      })
    }

  }

  def migrate(attemptOldId: Long, attemptNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(Some(attemptOldId))


    if (entities.nonEmpty) {
      entities.foreach(a => {
        val newId = activityStateTreeTQ.returning(activityStateTreeTQ.map(_.id)).insert(a.copy(attemptId = Some(attemptNewId)))
        activityStateMigration.migrate(false, a.id.get, newId)
      })
    }

  }

  private def getOld(aId: Option[Long] = None)(implicit s: JdbcBackend#Session): Seq[ActivityStateTreeModel] = {
    implicit val reader = GetResult[ActivityStateTreeModel](r => ActivityStateTreeModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextStringOption(), // TEXT null,
      r.nextStringOption(), // TEXT null,
      r.nextLongOption() // INTEGER null
    ))

    if(aId.isEmpty)
      StaticQuery.queryNA[ActivityStateTreeModel]("select * from Learn_LFActivityStateTree where attemptID is NULL").list
    else
      StaticQuery.queryNA[ActivityStateTreeModel](s"select * from Learn_LFActivityStateTree where attemptID = ${aId.get}").list
  }
}
