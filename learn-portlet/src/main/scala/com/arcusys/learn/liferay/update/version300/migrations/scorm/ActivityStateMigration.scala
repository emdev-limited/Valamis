package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateModel
import com.arcusys.valamis.persistence.impl.scorm.schema._
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ActivityStateMigration(val db: JdbcBackend#DatabaseDef,
                             val driver: JdbcProfile)
  extends ActivityStateTableComponent
    with ActivityStateNodeTableComponent
    with ActivityStateTreeTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  import driver.simple._

  val objectiveStateMigration= new ObjectiveStateMigration(db, driver)

  def migrate(isNode: Boolean, stateOldId: Long, stateNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = db.withSession{implicit s =>
      getOld(isNode, stateOldId)
    }

    entities.foreach { e =>
      val newEntity = if (isNode) e.copy(activityStateNodeId = Some(stateNewId))
      else e.copy(activityStateTreeId = Some(stateNewId))

      val newId = activityStateTQ.returning(activityStateTQ.map(_.id)).insert(newEntity)

      objectiveStateMigration.migrate(e.id.get, newId)
    }
  }

  private def getOld(isNode: Boolean, stateOldId: Long)(implicit s: JdbcBackend#Session): Seq[ActivityStateModel] = {
    implicit val reader = GetResult[ActivityStateModel](r => ActivityStateModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLongOption(),// INTEGER null,
      r.nextString(), //VARCHAR(3000) null,
      r.nextBoolean(), //BOOLEAN null,
      r.nextBoolean(), //BOOLEAN null,
      r.nextBooleanOption(), // BOOLEAN null,
      r.nextBigDecimalOption(), // NUMERIC(20,2),
      r.nextBigDecimal(), // NUMERIC(20,2),
      r.nextBigDecimal(), // NUMERIC(20,2),
      r.nextBigDecimal(), // NUMERIC(20,2),
      r.nextBigDecimal(), // NUMERIC(20,2),
      r.nextInt(), // INTEGER null,
      r.nextLongOption(), // INTEGER null,
      r.nextLongOption() // INTEGER null
    ))

    if(isNode)
      StaticQuery.queryNA[ActivityStateModel](s"select * from Learn_LFActivityState where activityStateNodeID = $stateOldId").list
    else
      StaticQuery.queryNA[ActivityStateModel](s"select * from Learn_LFActivityState where activityStateTreeID = $stateOldId").list

  }
}
