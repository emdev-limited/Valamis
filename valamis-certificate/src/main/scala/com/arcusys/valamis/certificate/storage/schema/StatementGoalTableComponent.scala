package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.StatementGoal
import com.arcusys.valamis.persistence.common.DbNameUtils.tblName
import com.arcusys.valamis.persistence.common.SlickProfile

trait StatementGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent{ self: SlickProfile =>

  import driver.simple._

  class StatementGoalTable(tag: Tag)
    extends Table[StatementGoal](tag, tblName("CERT_GOALS_STATEMENT"))
      with CertificateGoalBaseColumns {

    def verb = column[String]("VERB", O.Length(254, varying = true))
    def obj = column[String]("OBJ", O.Length(254, varying = true))

    def * = (goalId, certificateId, verb, obj) <> (StatementGoal.tupled, StatementGoal.unapply)

    def PK = goalPK("STATEMENT")
    def certificateFK = goalCertificateFK("STATEMENT")
    def statementGoalFK = goalFK("STATEMENT")
  }

  val statementGoals = TableQuery[StatementGoalTable]
}