package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage.CategoryStorage
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

trait CategoryService {

  def create(category: Category): Category

  def copyWithContent(id: Long, newTitle: String, newDescription: String): Category

  def update(id: Long, newTitle: String, newDescription: String): Unit

  def moveToCategory(id:Long, newCategoryId:Option[Long],courseId:Long)

  def moveToCourse(id:Long, courseId:Long,moveToRoot:Boolean)

  def getByID(id:Long):Option[Category]

  def getByTitle(name:String):Option[Category]

  def getByTitleAndCourseId(name:String, courseId: Long):Option[Category]

  def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Category]

  def deleteWithContent(id:Long)

  }

//TODO transaction support (ContentManager services)
class CategoryServiceImpl(implicit val bindingModule: BindingModule)
  extends CategoryService
  with Injectable {

  lazy val categories = inject[CategoryStorage]
  lazy val plainService = inject[PlainTextService]
  lazy val questionService = inject[QuestionService]


  override def create(category: Category): Category = {
      categories.create(category)
  }

  override def getByID(id:Long): Option[Category] = {
    categories.getById(id)
  }

  override def getByTitle(name:String):Option[Category] = {
    categories.getByTitle(name)
  }

  override def getByTitleAndCourseId(name:String, courseId: Long):Option[Category] = {
    categories.getByTitleAndCourseId(name, courseId)
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Category] = {
    categories.getByCategory(categoryId,courseId)
  }

  override def copyWithContent(oldCategoryId: Long, newTitle: String, newDescription: String): Category = {
    val category = categories.getById(oldCategoryId).get // <---

    copyWithContent(category.copy(title = newTitle, description = newDescription), category.categoryId)
  }

  private def copyWithContent(oldCategory: Category, newParentId: Option[Long]): Category = {

    val newCategory = create(oldCategory.copy(id = None, categoryId = newParentId))

    plainService.copyByCategory(oldCategory.id, newCategory.id, oldCategory.courseId)

    questionService.copyByCategory(oldCategory.id, newCategory.id, oldCategory.courseId)

    categories.getByCategory(oldCategory.id, oldCategory.courseId)
      .foreach(copyWithContent(_, newCategory.id))

    newCategory
  }

  override def update(id: Long, newTitle: String, newDescription: String): Unit = {
    categories.getById(id).foreach(category =>
      categories.update(category.copy(title = newTitle, description = newDescription))
    )
  }

  override def moveToCourse(id:Long, courseId:Long,moveToRoot:Boolean): Unit = {
    categories.getById(id).foreach(cat => {
      categories.moveToCourse(id, courseId, moveToRoot)
      moveRelatedContentToCourse(id,cat.courseId,courseId)
    }
    )

  }

  override def moveToCategory(id:Long, newCategoryId:Option[Long],courseId:Long):Unit = {
    val newCourseId = if (newCategoryId.isDefined) {
      categories.getById(newCategoryId.get).map(_.courseId).getOrElse(courseId)
    } else {
      courseId
    }

    categories.moveToCategory(id, newCategoryId, newCourseId)

    if (newCourseId!=courseId) {
      moveRelatedContentToCourse(id,courseId,newCourseId)
    }
  }

  private def moveRelatedContentToCourse(categoryId:Long,oldCourseId:Long,newCourseId:Long): Unit = {
    questionService.getByCategory(Some(categoryId), oldCourseId).foreach(q => questionService.moveToCourse(q.id.get, newCourseId, moveToRoot = false))
    plainService.getByCategory(Some(categoryId), oldCourseId).foreach(pt => plainService.moveToCourse(pt.id.get, newCourseId,moveToRoot = false))
    categories.getByCategory(Some(categoryId), oldCourseId).foreach(cat => moveToCourse(cat.id.get, newCourseId, moveToRoot = false))
  }

  override def deleteWithContent(id: Long): Unit = {
    //all related content will be delete automatically thanks to onDelete=ForeignKeyAction.Cascade option for FK
    //in ContentTableComponent classes
    //TODO delete content manually (in case of another storage impl)
    categories.delete(id)


  }

}