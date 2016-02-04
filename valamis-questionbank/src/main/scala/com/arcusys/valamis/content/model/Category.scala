package com.arcusys.valamis.content.model

case class Category(id: Option[Long],
                    title: String,
                    description: String,
                    categoryId: Option[Long],
                    courseId: Long
                     ) extends Content {
  def contentType = ContentType.Category
}
