package com.arcusys.valamis.persistence.impl.settings

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.settings.model.{Setting, SettingType}

import scala.slick.driver.JdbcProfile

/**
 * Created by Igor Borisov on 04.09.15.
 */
trait SettingTableComponent extends TypeMapper {self:SlickProfile =>
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
