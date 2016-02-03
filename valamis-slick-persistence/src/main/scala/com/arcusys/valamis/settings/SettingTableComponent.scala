package com.arcusys.valamis.settings

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.settings.model.{SettingType, Setting}

import scala.slick.driver.JdbcProfile

/**
 * Created by Igor Borisov on 04.09.15.
 */
trait SettingTableComponent {
  protected val driver: JdbcProfile
  import driver.simple._

  class SettingsTable(tag : Tag) extends Table[Setting](tag, tblName("SETTINGS")) {

    implicit val SettingTypeTypeMapper = MappedColumnType.base[SettingType.SettingType, String](
      s => s.toString,
      s => SettingType.withName(s)
    )

    def datakey = column[SettingType.SettingType]("DATAKEY", O.PrimaryKey, O.Length(128, true))
    def dataValue = column[String]("DATAVALUE", O.NotNull, O.Length(2048, true))

    def * = (datakey, dataValue) <> (Setting.tupled, Setting.unapply)
  }

  val settings = TableQuery[SettingsTable]
}
