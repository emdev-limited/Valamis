package com.arcusys.valamis.web.util

import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.Period
import org.scalatest.{FunSuiteLike, Matchers}

import scala.util.Success


class PeriodTypesTest extends FunSuiteLike with Matchers {

  test("should convert None to PeriodTypes.UNLIMITED") {
    PeriodTypes.fromJodaPeriod(None) shouldBe Success(PeriodTypes.UNLIMITED, None)
  }

  test("should convert P0Y0M2D to 2 days") {
    val jodaPeriod = new Period("P0Y0M2D")
    PeriodTypes.fromJodaPeriod(Some(jodaPeriod)) shouldBe Success(PeriodTypes.DAYS, Some(2))
  }

  test("should convert P2D to 2 days") {
    val jodaPeriod = new Period("P2D")
    PeriodTypes.fromJodaPeriod(Some(jodaPeriod)) shouldBe Success(PeriodTypes.DAYS, Some(2))
  }

  test("should convert P3W to 3 weeks") {
    val jodaPeriod = new Period("P3W")
    PeriodTypes.fromJodaPeriod(Some(jodaPeriod)) shouldBe Success(PeriodTypes.WEEKS, Some(3))
  }

  test("should convert P4M to 4 months") {
    val jodaPeriod = new Period("P4M")
    PeriodTypes.fromJodaPeriod(Some(jodaPeriod)) shouldBe Success(PeriodTypes.MONTH, Some(4))
  }

  test("should convert P5Y to 5 years") {
    val jodaPeriod = new Period("P5Y")
    PeriodTypes.fromJodaPeriod(Some(jodaPeriod)) shouldBe Success(PeriodTypes.YEAR, Some(5))
  }

  test("should fail with P3W2D") {
    val jodaPeriod = new Period("P3W2D")
    intercept[IllegalArgumentException] {
      PeriodTypes.fromJodaPeriod(Some(jodaPeriod)).get
    }.getMessage shouldBe "Only periods with exactly one field are supported"
  }

  test("should fail with PT3H") {
    val jodaPeriod = new Period("PT3H")
    intercept[IllegalArgumentException] {
      PeriodTypes.fromJodaPeriod(Some(jodaPeriod)).get
    }.getMessage shouldBe "Unsupported period type: hours"
  }

  test("should fail with P") {
    val jodaPeriod = new Period("P")
    intercept[IllegalArgumentException] {
      PeriodTypes.fromJodaPeriod(Some(jodaPeriod)).get
    }.getMessage shouldBe "Only periods with exactly one field are supported"
  }

  test("should fail with P0Y0M0D") {
    val jodaPeriod = new Period("P0Y0M0D")
    intercept[IllegalArgumentException] {
      PeriodTypes.fromJodaPeriod(Some(jodaPeriod)).get
    }.getMessage shouldBe "Only periods with exactly one field are supported"
  }

  test("should fail with P0Y0M-2D") {
    val jodaPeriod = new Period("P0Y0M-2D")
    intercept[IllegalArgumentException] {
      PeriodTypes.fromJodaPeriod(Some(jodaPeriod)).get
    }.getMessage shouldBe "Period value should be positive"
  }

}