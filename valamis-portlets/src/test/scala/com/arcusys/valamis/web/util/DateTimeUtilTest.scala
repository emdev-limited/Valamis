package com.arcusys.valamis.web.util

import org.joda.time.format.DateTimeFormat
import org.scalatest.FunSuiteLike


class DateTimeUtilTest extends FunSuiteLike {

  val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  test("should parse YYYY-mm-dd formatted string"){
    val date = DateTimeUtil.parseDate("2016-10-30")
    assert(fmt.print(date) == "2016-10-30 00:00:00")
  }

  test("should parse YYYY-mm-ddTHH:ii:ssZ formatted string"){
    val date = DateTimeUtil.parseDateWithTime("2016-10-30T12:13:14Z")
    assert(fmt.print(date) == "2016-10-30 12:13:14")
  }
}