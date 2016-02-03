package com.arcusys.learn.liferay.update

import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.valamis.core.{SlickDBInfo, SlickProfile}
import com.escalatesoft.subcut.inject.Injectable


trait SlickDBContext extends SlickProfile with Injectable {

  protected lazy val dbInfo = inject[SlickDBInfo]
  protected lazy val db = dbInfo.databaseDef
  lazy val driver = dbInfo.slickProfile
  protected lazy val O = driver.columnOptions

  implicit lazy val dialect = new Dialect(dbInfo.slickDriver)
}
