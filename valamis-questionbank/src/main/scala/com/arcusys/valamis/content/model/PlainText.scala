package com.arcusys.valamis.content.model

case class PlainText(id: Option[Long],
                     categoryId: Option[Long],
                     title: String,
                     text: String,
                     courseId: Long)
  extends Content {
  def contentType = ContentType.Text
}
