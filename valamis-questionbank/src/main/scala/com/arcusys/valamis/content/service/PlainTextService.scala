package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.exceptions.NoPlainTextException
import com.arcusys.valamis.content.model.{PlainTextNode, PlainText}
import com.arcusys.valamis.content.storage.{CategoryStorage, PlainTextStorage}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}

trait PlainTextService {

  def getById(id: Long): PlainText

  def create(plainText: PlainText): PlainText

  def delete(id: Long): Unit

  def update(id: Long, title: String, text: String): Unit

  def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[PlainText]

  def getByCategory(categoryId: Option[Long], courseId: Long): Seq[PlainText]

  def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean)

  def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long)

  def getPlainTextNodeById(id: Long): PlainTextNode

}

class PlainTextServiceImpl(implicit val bindingModule: BindingModule)
  extends PlainTextService
  with Injectable {

  lazy val plainTexts = inject[PlainTextStorage]

  lazy val cats = inject[CategoryStorage]

  override def getById(id: Long): PlainText = {
    plainTexts.getById(id).getOrElse(throw new NoPlainTextException(id))
  }

  override def getPlainTextNodeById(id: Long): PlainTextNode = {
    plainTexts.getById(id).fold(throw new NoPlainTextException(id)) { q =>
      new TreeBuilder().getPlainTextNode(q)
    }
  }

  override def create(plainText: PlainText): PlainText = {
    plainTexts.create(plainText)
  }

  override def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[PlainText] = {
    for (plainText <- plainTexts.getByCategory(categoryId, courseId))
      yield plainTexts.create(plainText.copy(id = None, categoryId = newCategoryId))
  }

  override def update(id: Long, title: String, text: String): Unit = {
    val entity = plainTexts.getById(id).get
    plainTexts.update(entity.copy(title = title, text = text))
  }

  override def delete(id: Long): Unit = {
    plainTexts.delete(id)
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean) = {
    plainTexts.moveToCourse(id, courseId, moveToRoot)
  }

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long) = {
    val newCourseId = if (newCategoryId.isDefined) {
      cats.getById(newCategoryId.get).map(_.courseId).getOrElse(courseId)
    } else {
      courseId
    }
    plainTexts.moveToCategory(id, newCategoryId, newCourseId)
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[PlainText] = {
    plainTexts.getByCategory(categoryId, courseId)
  }
}
