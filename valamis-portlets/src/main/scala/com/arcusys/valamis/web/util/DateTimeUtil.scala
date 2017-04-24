package com.arcusys.valamis.web.util

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat


object DateTimeUtil {

  /**
    * Parse yyyy-MM-dd formatted date string
    */
  def parseDate(str: String): DateTime = {
    val parser = ISODateTimeFormat.dateParser()
    parser.parseDateTime(str)
  }

  /**
    * Parse yyyy-MM-ddTHH:mm:ssZ formatted date string
    */
  def parseDateWithTime(str: String): DateTime = {
    val parser = ISODateTimeFormat.dateTimeParser()
    parser.parseDateTime(str)
  }
}