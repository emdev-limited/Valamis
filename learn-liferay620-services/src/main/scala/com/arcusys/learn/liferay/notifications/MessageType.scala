package com.arcusys.learn.liferay.notifications

object MessageType extends Enumeration {
  type MessageType = Value

  val Like = Value("like")
  val Comment = Value("comment")
  val Grade = Value("grade")
  val PackageGrade = Value("package_grade")
  val StatementComment = Value("statement_comment")
}
