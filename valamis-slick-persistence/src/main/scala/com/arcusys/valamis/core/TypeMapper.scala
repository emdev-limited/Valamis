package com.arcusys.valamis.core

import java.sql.{Date, Timestamp}

import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.util.serialization.JsonHelper
import org.joda.time.{DateTime, Duration, LocalDate}

import scala.slick.driver.JdbcDriver.simple._

/**
 * Custom Type mappers for Slick.
 */
trait TypeMapper {

  /** Type mapper for [[org.joda.time.DateTime]] */
  implicit val dateTimeMapper: BaseColumnType[DateTime] = MappedColumnType.base[DateTime, Timestamp](
    dt => {
      if (dt != null) 
      	new Timestamp(dt.getMillis) 
      else 
        null
    },
    ts => {
      new DateTime(ts.getTime)
    }
  )

  /** Type mapper for [[org.joda.time.LocalDate]] */
  implicit val localDateMapper: BaseColumnType[LocalDate] = MappedColumnType.base[LocalDate, Date](
    dt => new Date(dt.toDate.getTime),
    d => new LocalDate(d.getTime)
  )

  /** Type mapper for [[org.joda.time.Duration]] */
  implicit val durationTypeMapper: BaseColumnType[Duration] = MappedColumnType.base[Duration, Long](
    d => d.getMillis,
    l => Duration.millis(l)
  )

  implicit val contentTypeMapper = MappedColumnType.base[ContentType.Type, String](
    v => v.toString,
    s => ContentType.withName(s)
  )
}