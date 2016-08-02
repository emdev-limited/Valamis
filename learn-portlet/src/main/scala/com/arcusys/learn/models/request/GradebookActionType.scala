package com.arcusys.learn.models.request

object GradebookActionType extends Enumeration {
  type GradebookActionType = Value

  val All = Value("ALL")
  val Grades = Value("GRADES")
  val LastModified = Value("LAST_MODIFIED")
  val Review = Value("REVIEW")
  val Statements = Value("STATEMENTS")
}
