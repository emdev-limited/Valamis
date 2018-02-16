package com.arcusys.valamis.model

import org.joda.time.{DateTime, DurationFieldType, Period => JPeriod}

import scala.util.{Failure, Success, Try}

case class Period(periodType: PeriodTypes.Value, value: Int)

object Period{
  def unlimited: Period = Period(PeriodTypes.UNLIMITED, 0)
}

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

  def toJodaPeriod(periodType: PeriodType, value: Int): Option[JPeriod] = {
    periodType match {
      case PeriodTypes.DAYS      => Some(JPeriod.days(value))
      case PeriodTypes.WEEKS     => Some(JPeriod.weeks(value))
      case PeriodTypes.MONTH     => Some(JPeriod.months(value))
      case PeriodTypes.YEAR      => Some(JPeriod.years(value))
      case PeriodTypes.UNLIMITED => None
    }
  }

  def fromJodaPeriod(period: Option[JPeriod]): Try[(PeriodType, Option[Int])] = {
    period match {
      case None => Success((PeriodTypes.UNLIMITED, None))
      case Some(p) =>
        val pValues = p.getValues.zipWithIndex.collect {
          case (v, index) if v != 0 => (v, p.getFieldType(index))
        }
        if (pValues.length != 1) {
          Failure(new IllegalArgumentException("Only periods with exactly one field are supported"))
        } else {
          val (value, tpe) = pValues.head
          if (value < 0 ) {
            Failure(new IllegalArgumentException("Period value should be positive"))
          } else {
            val periodType = tpe match {
              case t if t == DurationFieldType.days() => PeriodTypes.DAYS
              case t if t == DurationFieldType.weeks() => PeriodTypes.WEEKS
              case t if t == DurationFieldType.months() => PeriodTypes.MONTH
              case t if t == DurationFieldType.years() => PeriodTypes.YEAR
              case t => return Failure(new IllegalArgumentException("Unsupported period type: " + t))
            }

            Success((periodType, Some(value)))
          }
        }
    }
  }

  def getEndDate(periodType: PeriodType, value: Option[Int], startDate: DateTime): DateTime =
    getEndDate(periodType, value.getOrElse(0), startDate)

  def getEndDate(periodType: Option[PeriodType], value: Option[Int], startDate: DateTime): DateTime =
    getEndDate(periodType.getOrElse(PeriodTypes.UNLIMITED), value.getOrElse(0), startDate)

  def getEndDate(periodType: PeriodType, value: Int, startDate: DateTime): DateTime = periodType match {
    case PeriodTypes.DAYS      => startDate.plusDays(value)
    case PeriodTypes.WEEKS     => startDate.plusWeeks(value)
    case PeriodTypes.MONTH     => startDate.plusMonths(value)
    case PeriodTypes.YEAR      => startDate.plusYears(value)
    case PeriodTypes.UNLIMITED => startDate.plusYears(99999)
  }

  def getEndDateOption(periodType: PeriodType, value: Int, startDate: DateTime): Option[DateTime] = periodType match {
    case PeriodTypes.UNLIMITED => None
    case _ => Some(getEndDate(periodType, value, startDate))
  }
}
