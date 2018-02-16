package com.arcusys.valamis.util.serialization

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

import scala.util.Try

object DateTimeSerializer extends CustomSerializer[DateTime](implicit format => ( {
  case JString(str) =>
    Try(ISODateTimeFormat.dateTimeParser().parseDateTime(str)).getOrElse(DateTime.now)
  case _ =>
    DateTime.now
}, {
  case dt: DateTime => JString(dt.toString)
}))

