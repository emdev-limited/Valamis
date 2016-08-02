package com.arcusys.valamis.certificate.model

object AssignmentSortBy extends Enumeration {
  type AssignmentSortBy = Value
  val Title, Deadline = Value
  def apply(v: String): AssignmentSortBy = v.toLowerCase match {
    case "title"    => Title
    case "deadline" => Deadline
    case _          => throw new IllegalArgumentException()
  }
}