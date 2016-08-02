package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.{ObjectiveModel, ObjectiveStateModel}
import com.arcusys.valamis.persistence.impl.scorm.schema._
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ObjectiveStateMigration(val db: JdbcBackend#DatabaseDef,
                              val driver: JdbcProfile)
  extends ObjectiveStateTableComponent
    with ActivityStateTableComponent
    with ObjectiveTableComponent
    with ActivityStateNodeTableComponent
    with ActivityStateTreeTableComponent
    with SequencingTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  import driver.simple._

  def migrate(stateOldId: Long, stateNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(stateOldId)

    if (entities.nonEmpty) {
      entities.foreach( a => {
        val newId = objectiveStateTQ.returning(objectiveStateTQ.map(_.id)).insert(a.copy(activityStateId = stateNewId, objectiveId = None))
      })
    }
  }

  private def getOld(stateId: Long)(implicit s: JdbcBackend#Session): Seq[ObjectiveStateModel] = {
    implicit val reader = GetResult[ObjectiveStateModel](r => ObjectiveStateModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextBooleanOption(), // satisfied BOOLEAN null,
      r.nextBigDecimalOption(), // normalizedMeasure NUMERIC(20,2),
      r.nextStringOption(), // mapKey VARCHAR(3000) null,
      r.nextLong(), // activityStateID INTEGER null,
      r.nextLongOption() // objectiveID INTEGER null
    ))

    StaticQuery.queryNA[ObjectiveStateModel](s"select * from Learn_LFObjectiveState where activityStateID = $stateId").list
  }
}
