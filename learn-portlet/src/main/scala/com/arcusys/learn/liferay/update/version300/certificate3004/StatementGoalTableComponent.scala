package com.arcusys.learn.liferay.update.version300.certificate3004

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait StatementGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent {self: SlickProfile =>

  import driver.simple._

  case class StatementGoal(goalId: Long,
                           certificateId: Long,
                           verb: String,
                           obj: String)

  class StatementGoalTable(tag: Tag) extends Table[StatementGoal](tag, tblName("CERT_GOALS_STATEMENT")) {
    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def verb = column[String]("VERB", O.Length(254, true))
    def obj = column[String]("OBJ", O.Length(254, true))

    def * = (goalId, certificateId, verb, obj) <> (StatementGoal.tupled, StatementGoal.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_STATEMENT"), (goalId, certificateId))
    def certificateFK = foreignKey(fkName("GOALS_STATEMENT_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
    def goalFK = foreignKey(fkName("GOALS_STATEMENT_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val statementGoals = TableQuery[StatementGoalTable]
}