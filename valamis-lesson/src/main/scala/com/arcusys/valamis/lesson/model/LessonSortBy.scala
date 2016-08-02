package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.model.{Order, SortBy}

object LessonSortBy extends Enumeration {
  val Name, Date, Default = Value

  def apply(v: String): LessonSortBy.Value = v.toLowerCase() match {
    case "name" => Name
    case "date" => Date
    case "default" => Default
    case _      => throw new IllegalArgumentException()
  }
}

case class LessonSort(sortBy: LessonSortBy.Value, order: Order.Value) extends SortBy(sortBy, order)
