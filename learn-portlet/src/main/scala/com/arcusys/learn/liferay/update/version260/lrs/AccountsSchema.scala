package com.arcusys.learn.liferay.update.version260.lrs

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.core.{LongKeyTableComponent, SlickProfile}

trait AccountsSchema extends LongKeyTableComponent with SlickProfile {

  import driver.simple._

  type AccountsEntity = (Option[String], Option[String])

  class AccountsTable(tag: Tag) extends Table[AccountsEntity](tag, "lrs_accounts") {

    def key = column[Long]("key", O.PrimaryKey, O.AutoInc)

    def name = column[Option[String]]("name", O.DBType(varCharMax))

    def homePage = column[Option[String]]("homePage", O.DBType(varCharMax))

    def * = (name, homePage)
  }

  val accounts = TableQuery[AccountsTable]
}
