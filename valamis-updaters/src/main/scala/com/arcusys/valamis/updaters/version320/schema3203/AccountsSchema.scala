package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile}

trait AccountsSchema extends LongKeyTableComponent with SlickProfile {

  import driver.simple._

  type AccountsEntity = (Long, Option[String], Option[String])

  class AccountsTable(tag: Tag) extends Table[AccountsEntity](tag, "lrs_accounts") {

    def key = column[Long]("key", O.PrimaryKey, O.AutoInc)

    def name = column[Option[String]]("name", O.DBType(varCharMax))

    def homePage = column[Option[String]]("homePage", O.DBType(varCharMax))

    def * = (key, name, homePage)
  }

  val accounts = TableQuery[AccountsTable]
}
