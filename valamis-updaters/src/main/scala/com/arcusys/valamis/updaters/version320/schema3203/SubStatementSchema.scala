package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.DbNameUtils._


trait SubStatementSchema {
  self: SlickProfile =>

  import driver.api._

  type SubStatementRow = (Long, Long, Long, String, String)

  class SubStatementsTable(tag: Tag) extends Table[SubStatementRow](tag, "lrs_subStatements") {
    override def * = (key, statementObjectKey, actorKey, verbId, verbDisplay)

    def key = column[Long]("key", O.PrimaryKey, O.AutoInc)
    def statementObjectKey = column[Long]("statementObject")
    def actorKey = column[Long]("actorId")
    def verbId = column[String]("verbId", O.SqlType(varCharMax))
    def verbDisplay = column[String]("verbDisplay", O.SqlType(varCharMax))

//    def actor = foreignKey(fkName("subStmnt2actor"), actorKey, TableQuery[ActorsTable])(_.key)
//    def statementObject = foreignKey(fkName("subSstmnt2stmntObj"), statementObjectKey, TableQuery[StatementObjectsTable])(_.key)
  }

  lazy val subStatements = TableQuery[SubStatementsTable]

}
