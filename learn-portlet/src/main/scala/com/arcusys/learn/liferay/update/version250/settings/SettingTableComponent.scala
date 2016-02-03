package com.arcusys.learn.liferay.update.version250.settings

import com.arcusys.valamis.core.DbNameUtils._

import scala.slick.driver.JdbcProfile

/**
 * Created by Igor Borisov on 03.09.15.
 */
trait SettingTableComponent {
  protected val driver: JdbcProfile
  import driver.simple._

  type Setting = (String, String)
  class SettingsTable(tag : Tag) extends Table[Setting](tag, tblName("SETTINGS")) {

    def datakey = column[String]("DATAKEY", O.PrimaryKey, O.Length(128, true))
    def dataValue = column[String]("DATAVALUE", O.NotNull, O.Length(2048, true))

    def * = (datakey, dataValue)
  }

  val settings = TableQuery[SettingsTable]
}
