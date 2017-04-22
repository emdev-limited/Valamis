package com.arcusys.valamis.slide.model

object SlideSetSortBy extends Enumeration {
  type SlideSetSortBy = Value
  val Name, ModifiedDate = Value
  def apply(v: String): SlideSetSortBy = v.toLowerCase match {
    case "name"         => Name
    case "modifieddate" => ModifiedDate
    case _              => throw new IllegalArgumentException()
  }
}

