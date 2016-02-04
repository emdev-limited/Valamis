package com.arcusys.valamis.settings

import com.arcusys.valamis.settings.model.{Setting, SettingType}
import com.arcusys.valamis.settings.storage.SettingStorage

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 * Created by Igor Borisov on 04.09.15.
 */
class SettingStorageImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SettingStorage with SettingTableComponent{
  import driver.simple._

  implicit val SettingTypeTypeMapper = MappedColumnType.base[SettingType.SettingType, String](
    s => s.toString,
    s => SettingType.withName(s)
  )

  override def getByKey(key: SettingType.Value): Option[Setting] = db.withSession { implicit s =>
    settings.filter(_.datakey === key).firstOption
  }

  override def modify(key: SettingType.Value, value: String): Unit = db.withTransaction { implicit s =>
    val setting = Setting(key, value)

    val updatedCount = settings
      .filter(_.datakey === key)
      .update(setting)

    if (updatedCount == 0)
      settings.insert(setting)
  }
}
