package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.GlblObjectiveStateModel
import com.arcusys.valamis.persistence.impl.scorm.schema.GlblObjectiveStateTableComponent
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class GlblObjectiveStateMigration(val db: JdbcBackend#DatabaseDef,
                                  val driver: JdbcProfile)
  extends GlblObjectiveStateTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val grades = getOldGrades


    if (grades.nonEmpty) {
      glblObjectiveStateTQ ++= grades
    }

  }

  private def getOldGrades(implicit s: JdbcBackend#Session): Seq[GlblObjectiveStateModel] = {
    implicit val reader = GetResult[GlblObjectiveStateModel](r => GlblObjectiveStateModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextBooleanOption(), // satisfied BOOLEAN null,
      r.nextBigDecimalOption(), // normalizedMeasure NUMERIC(20,2),
      r.nextBooleanOption(), // attemptCompleted BOOLEAN null,
      r.nextString(), // mapKey VARCHAR(75) null,
      r.nextLong() // treeID INTEGER null
    ))

    StaticQuery.queryNA[GlblObjectiveStateModel]("select * from Learn_LFGlblObjectiveState").list
  }
}
