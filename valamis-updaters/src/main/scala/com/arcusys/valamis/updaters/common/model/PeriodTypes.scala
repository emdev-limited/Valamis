package com.arcusys.valamis.updaters.common.model

object PeriodTypes extends Enumeration {
  type PeriodType = Value

  val UNLIMITED, YEAR, MONTH, WEEKS, DAYS = Value

  def parse(value: String) = value.toLowerCase() match {
    case "unlimited" => UNLIMITED
    case "year"      => YEAR
    case "month"     => MONTH
    case "weeks"     => WEEKS
    case "days"      => DAYS
    case _           => UNLIMITED
  }

  def apply(value: String) = parse(value)

  def apply(value: Option[String]) = value match {
    case Some(v) => parse(v)
    case None    => UNLIMITED
  }

}
