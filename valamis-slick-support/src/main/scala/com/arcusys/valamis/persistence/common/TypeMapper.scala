package com.arcusys.valamis.persistence.common

import com.arcusys.valamis.persistence.common.joda.JodaDateTimeMapper
import slick.driver.JdbcDriver
import slick.driver.JdbcDriver.simple._

/**
 * Custom Type mappers for Slick.
 */
trait TypeMapper { self:SlickProfile =>

  implicit lazy val jodaMapper = new JodaDateTimeMapper(driver.asInstanceOf[JdbcDriver]).typeMapper

  def enumerationMapper[T <: Enumeration](enum: T) = MappedColumnType.base[enum.Value, String](
    e => e.toString,
    s => enum.withName(s)
  )

  def enumerationIdMapper[T <: Enumeration](enum: T) = MappedColumnType.base[enum.Value, Int](
    e => e.id,
    v => enum(v)
  )
}