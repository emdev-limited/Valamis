package com.arcusys.valamis.settings.storage

import com.arcusys.valamis.settings.model.{ Setting, SettingType }

/**
 * User: Yulia.Glushonkova
 * Date: 02.10.13
 */
trait SettingStorage {
  def getByKey(key: SettingType.Value): Option[Setting]
  def modify(key: SettingType.Value, value: String)
}
