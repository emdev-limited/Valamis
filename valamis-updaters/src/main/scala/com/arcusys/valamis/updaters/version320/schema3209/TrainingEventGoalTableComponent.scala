package com.arcusys.valamis.updaters.version320.schema3209

import com.arcusys.valamis.persistence.common.DbNameUtils.{fkName, pkName, tblName}
import com.arcusys.valamis.persistence.common.SlickProfile



trait TrainingEventGoalTableComponent
  extends CertificateTableComponent
  with CertificateGoalTableComponent{ self: SlickProfile =>

  import driver.api._

  class TrainingEventGoalTable(tag: Tag)
    extends Table[TrainingEventGoal](tag, tblName("CERT_GOALS_TR_EVENT")) {

    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def eventId = column[Long]("EVENT_ID")

    def * = (goalId, certificateId, eventId) <> (TrainingEventGoal.tupled, TrainingEventGoal.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_TR_EVENT"), (goalId, certificateId))
    def certificateFK = foreignKey(fkName("GOALS_TR_EVENT_TO_CERT"), certificateId, certificates)(x => x.id,
      onDelete = ForeignKeyAction.NoAction)
    def eventGoalFK = foreignKey(fkName("GOALS_TR_EVENT_TO_GOAL"), goalId, certificateGoals)(x => x.id,
      onDelete = ForeignKeyAction.Cascade)
  }

  val trainingEventGoals = TableQuery[TrainingEventGoalTable]
}



