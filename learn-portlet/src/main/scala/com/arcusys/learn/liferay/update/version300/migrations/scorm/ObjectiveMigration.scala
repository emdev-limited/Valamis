package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.{OptionFilterSupport, SlickProfile}
import com.arcusys.valamis.persistence.impl.scorm.model.{ObjectiveModel, ObjectiveStateModel}
import com.arcusys.valamis.persistence.impl.scorm.schema._
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ObjectiveMigration(val db: JdbcBackend#DatabaseDef,
                         val driver: JdbcProfile)
  extends ObjectiveTableComponent
    with SequencingTableComponent
    with ObjectiveStateTableComponent
    with ActivityStateTableComponent
    with ActivityStateNodeTableComponent
    with ActivityStateTreeTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile
    with OptionFilterSupport {

  import driver.simple._

  val objectiveMapMigration = new ObjectiveMapMigration(db, driver)

  def migrate(seqOldId: Long, seqNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(seqOldId)


    if (entities.nonEmpty) {
      entities.foreach { e =>
        val newEntity = e.copy(sequencingId = seqNewId)
        val newId = objectiveTQ.returning(objectiveTQ.map(_.id)).insert(newEntity)
        objectiveMapMigration.migrate(e.id.get, newId)
        updateObjectiveId(e.id.get, newId)
      }
    }
  }

  private def updateObjectiveId(oldId: Long, newId: Long)(implicit s: JdbcBackend#Session): Unit = {
    implicit val reader = GetResult[ObjectiveStateModel](r => ObjectiveStateModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextBooleanOption(), // satisfied BOOLEAN null,
      r.nextBigDecimalOption(), // normalizedMeasure NUMERIC(20,2),
      r.nextStringOption(), // mapKey VARCHAR(3000) null,
      r.nextLong(), // activityStateID INTEGER null,
      r.nextLongOption() // objectiveID INTEGER null
    ))

    val objectiveStates = StaticQuery.queryNA[ObjectiveStateModel](s"select * from Learn_LFObjectiveState where objectiveID = $oldId").list

    objectiveStates.map { o =>
      val newObject = o.copy(objectiveId = Some(newId))

      objectiveStateTQ.filter(ob => optionFilter(ob.mapKey, o.mapKey) &&
        ob.activityStateId === o.activityStateId && optionFilter(ob.satisfied, o.satisfied) &&
        optionFilter(ob.normalizedMeasure, o.normalizedMeasure)).map(_.update).update(newObject)
    }
  }

  private def getOld(seqId: Long)(implicit s: JdbcBackend#Session): Seq[ObjectiveModel] = {
    implicit val reader = GetResult[ObjectiveModel](r => ObjectiveModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // sequencingID INTEGER null,
      r.nextBooleanOption(), // satisfiedByMeasure BOOLEAN null,
      r.nextStringOption(), // identifier VARCHAR(3000) null,
      r.nextBigDecimal(), // minNormalizedMeasure NUMERIC(20,2),
      r.nextBooleanOption() // isPrimary BOOLEAN null
    ))

    StaticQuery.queryNA[ObjectiveModel](s"select * from Learn_LFObjective where sequencingID = $seqId").list
  }
}
