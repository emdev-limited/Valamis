package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.exceptions.NoCategoryException
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage.{CategoryStorage, PlainTextStorage, QuestionStorage}
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits.global

trait CategoryService {

  def create(category: Category): Category

  def copyWithContent(id: Long, newTitle: String, newDescription: String): Category

  def update(id: Long, newTitle: String, newDescription: String): Unit

  def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long): Unit

  def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean): Unit

  def getByID(id: Long): Option[Category]

  def getByTitle(name: String): Option[Category]

  def getByTitleAndCourseId(name: String, courseId: Long): Option[Category]

  def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Category]

  def deleteWithContent(id: Long): Unit

}

class CategoryServiceImpl(implicit val bindingModule: BindingModule)
  extends CategoryService
    with Injectable {

  lazy val categories = inject[CategoryStorage]
  lazy val questionStorage = inject[QuestionStorage]
  lazy val plainTextStorage = inject[PlainTextStorage]

  lazy val plainTextService = inject[PlainTextService]
  lazy val questionService = inject[QuestionService]

  lazy val dbLayer = inject[DatabaseLayer]

  import DatabaseLayer._


  override def create(category: Category): Category = dbLayer.execSync(categories.create(category))

  override def getByID(id: Long): Option[Category] = dbLayer.execSync(categories.getById(id))

  override def getByTitle(name: String): Option[Category] = dbLayer.execSync(categories.getByTitle(name))

  override def getByTitleAndCourseId(name: String, courseId: Long): Option[Category] = dbLayer.execSync {
    categories.getByTitleAndCourseId(name, courseId)
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Category] = dbLayer.execSync {
    categories.getByCategory(categoryId, courseId)
  }

  override def copyWithContent(oldCategoryId: Long, newTitle: String, newDescription: String): Category =
    dbLayer.execSyncInTransaction {
      categories.getById(oldCategoryId) ifSomeThen { category =>
        copyWithContent(category.copy(title = newTitle, description = newDescription), category.categoryId)
      } map (_.getOrElse(throw new NoCategoryException(oldCategoryId)))
    }

  private def copyWithContent(oldCategory: Category, newParentId: Option[Long]): DBIO[Category] = {
    for {
      newCategory <- categories.create(oldCategory.copy(id = None, categoryId = newParentId))
      _ <- plainTextService.copyByCategoryAction(oldCategory.id, newCategory.id, oldCategory.courseId)
      _ <- questionService.copyByCategoryAction(oldCategory.id, newCategory.id, oldCategory.courseId)

      otherCats <- categories.getByCategory(oldCategory.id, oldCategory.courseId)
      _ <- sequence(otherCats.map { otherCat => copyWithContent(otherCat, newCategory.id) })
    } yield newCategory
  }

  override def update(id: Long, newTitle: String, newDescription: String): Unit = dbLayer.execSync {
    categories.getById(id).ifSomeThen { cat =>
      categories.update(cat.copy(title = newTitle, description = newDescription))
    }
  }

  private def moveToCourseAction(id: Long, courseId: Long, moveToRoot: Boolean): DBIO[Option[Unit]] = {
    categories.getById(id).ifSomeThen { cat =>
      categories.moveToCourse(id, courseId, moveToRoot) andThen
        moveRelatedContentToCourseAction(id, cat.courseId, courseId)
    }
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean): Unit = dbLayer.execSyncInTransaction {
    moveToCourseAction(id, courseId, moveToRoot)
  }

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long): Unit =
    dbLayer.execSyncInTransaction {
      if (newCategoryId.isDefined) {
        for {
          newCourseId <- categories.getById(newCategoryId.get).map(_.map(_.courseId).getOrElse(courseId))
          _ <- categories.moveToCategory(id, newCategoryId, newCourseId)
          _ <- if (newCourseId != courseId) {
            moveRelatedContentToCourseAction(id, courseId, newCourseId)
          } else {
            DBIO.successful()
          }
        } yield ()
      } else {
        categories.moveToCategory(id, newCategoryId, courseId)
      }
    }

  private def moveRelatedContentToCourseAction(categoryId: Long, oldCourseId: Long, newCourseId: Long) =
    for {
      questions <- questionStorage.getByCategory(Some(categoryId), oldCourseId)
      _ <- sequence(questions.map {q => questionService.moveToCourseAction(q.id.get, newCourseId, moveToRoot = false) })

      plainTexts <- plainTextStorage.getByCategory(Some(categoryId), oldCourseId)
      _ <- sequence(plainTexts.map { pt => plainTextService.moveToCourseAction(pt.id.get, newCourseId, moveToRoot = false) })

      cats <- categories.getByCategory(Some(categoryId), oldCourseId)
      _ <- sequence(cats.map { cat => moveToCourseAction(cat.id.get, newCourseId, moveToRoot = false) })
    } yield ()

  override def deleteWithContent(id: Long): Unit = {
    //all related content will be delete automatically thanks to onDelete=ForeignKeyAction.Cascade option for FK
    //in ContentTableComponent classes
    //TODO delete content manually (in case of another storage impl)
    dbLayer.execSyncInTransaction(categories.delete(id))
  }

}