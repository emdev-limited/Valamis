package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.DbNameUtils._


trait StatementObjectSchema {
  self: SlickProfile =>

  import driver.api._

  type StatementObjectRow = (Long, String)
  class StatementObjectsTable(tag: Tag) extends Table[StatementObjectRow](tag, "lrs_statementObjects") {

    def key = column[Long]("key", O.PrimaryKey, O.AutoInc)
    def objectType = column[String]("objectType", O.SqlType(varCharMax))

    def * = (key, objectType)
  }

  lazy val statementObjects = TableQuery[StatementObjectsTable]
}
