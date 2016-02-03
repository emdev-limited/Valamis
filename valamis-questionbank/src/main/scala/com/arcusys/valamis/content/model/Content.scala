package com.arcusys.valamis.content.model

import ContentType.ContentType

object ContentType extends Enumeration {
  type ContentType = Value
  val Category, Text, Question = Value
}


trait Content {
  //def id: Long
  def title: String
  //def description: String
  def categoryId: Option[Long]
  def courseId: Long
  def contentType: ContentType
}
