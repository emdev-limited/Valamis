package com.arcusys.valamis.model

case class RangeResult[+T](
  total: Long,
  records: Seq[T]) {
  def map[T2](f: T => T2): RangeResult[T2] = {
    RangeResult(total, records.map(f(_)))
  }
}

case class SkipTake(skip: Int, take: Int)

object Order extends Enumeration {
  def apply(from: Boolean) = if(from) Asc else Desc
  val Asc, Desc = Value
}

abstract class SortBy[T](sortBy: T, order: Order.Value)