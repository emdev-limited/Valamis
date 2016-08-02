package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.exceptions.NoPlainTextException
import com.arcusys.valamis.content.model.{PlainText, PlainTextNode}
import com.arcusys.valamis.content.storage.{CategoryStorage, PlainTextStorage}
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits.global

trait PlainTextService {

  def getById(id: Long): PlainText

  def create(plainText: PlainText): PlainText

  def delete(id: Long): Unit

  def update(id: Long, title: String, text: String): Unit

  private[content] def copyByCategoryAction(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): DBIO[Seq[PlainText]]

  def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[PlainText]

  def getByCategory(categoryId: Option[Long], courseId: Long): Seq[PlainText]

  private[content] def moveToCourseAction(id: Long, courseId: Long, moveToRoot: Boolean): DBIO[Int]

  def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean)

  def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long)

  def getPlainTextNodeById(id: Long): PlainTextNode

}

class PlainTextServiceImpl(implicit val bindingModule: BindingModule)
  extends PlainTextService
    with Injectable {

  lazy val plainTexts = inject[PlainTextStorage]

  lazy val cats = inject[CategoryStorage]
  lazy val dbLayer = inject[DatabaseLayer]

  import DatabaseLayer._

  override def getById(id: Long): PlainText =
    dbLayer.execSync(plainTexts.getById(id)).getOrElse(throw new NoPlainTextException(id))

  override def getPlainTextNodeById(id: Long): PlainTextNode =
    dbLayer.execSync(plainTexts.getById(id)).fold(throw new NoPlainTextException(id)) { q =>
      new TreeBuilder().getPlainTextNode(q)
    }

  override def create(plainText: PlainText): PlainText = dbLayer.execSync(plainTexts.create(plainText))

  def copyByCategoryAction(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): DBIO[Seq[PlainText]] =
    for {
      srcItems <- plainTexts.getByCategory(categoryId, courseId)
      newItems <- sequence(srcItems.map { pt => plainTexts.create(pt.copy(id = None, categoryId = newCategoryId)) })
    } yield newItems

  override def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[PlainText] =
    dbLayer.execSyncInTransaction(copyByCategoryAction(categoryId, newCategoryId, courseId))

  override def update(id: Long, title: String, text: String): Unit = dbLayer.execSync {
    plainTexts.getById(id).ifSomeThen { pt =>
      plainTexts.update(pt.copy(title = title, text = text))
    }
  }

  override def delete(id: Long): Unit = dbLayer.execSync(plainTexts.delete(id))

  override def moveToCourseAction(id: Long, courseId: Long, moveToRoot: Boolean) =
    plainTexts.moveToCourse(id, courseId, moveToRoot)

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean) = dbLayer.execSync {
    plainTexts.moveToCourse(id, courseId, moveToRoot)
  }

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long): Unit = dbLayer.execSync {
    if (newCategoryId.isDefined) {
      for {
        newCourseId <- cats.getById(newCategoryId.get).map(_.map(_.courseId).getOrElse(courseId))
        _ <- plainTexts.moveToCategory(id, newCategoryId, newCourseId)
      } yield ()
    } else {
      plainTexts.moveToCategory(id, newCategoryId, courseId)
    }
  }


  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[PlainText] = dbLayer.execSync {
    plainTexts.getByCategory(categoryId, courseId)
  }
}
