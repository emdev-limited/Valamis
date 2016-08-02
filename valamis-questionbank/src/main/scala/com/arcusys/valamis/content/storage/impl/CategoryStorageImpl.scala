package com.arcusys.valamis.content.storage.impl

import com.arcusys.valamis.content.model.Category
import com.arcusys.valamis.content.storage.CategoryStorage
import com.arcusys.valamis.content.storage.impl.schema.ContentTableComponent
import com.arcusys.valamis.persistence.common.{OptionFilterSupport3, SlickProfile, DatabaseLayer}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * *
  * Created by pkornilov on 23.10.15.
  */

class CategoryStorageImpl(val db: JdbcBackend#DatabaseDef,
                          val driver: JdbcProfile)
  extends CategoryStorage
    with ContentTableComponent
    with OptionFilterSupport3
    with SlickProfile {

  import driver.api._
  import DatabaseLayer._

  override def getByTitle(name: String) =
    questionCategories.filter(_.title === name).result.headOption


  override def getByTitleAndCourseId(name: String, courseId: Long) =
    questionCategories.filter(q => q.title === name && q.courseId === courseId).result.headOption


  override def getById(id: Long) =
    questionCategories.filter(_.id === id).result.headOption

  override def update(category: Category) =
    questionCategories.filter(_.id === category.id).map(_.update).update(category)

  override def delete(id: Long) = deleteTreeAction(id)

  private def deleteTreeAction(id: Long): DBIO[Int] = {
    val deleteChildrenAction = for {
      children <- questionCategories.filter(_.parentId === id).map(_.id).result
      _ <- sequence(children.map(deleteTreeAction))
    } yield ()

    deleteChildrenAction andThen
      questionCategories.filter(_.id === id).delete
  }

  override def create(category: Category) =
    (questionCategories returning questionCategories.map(_.id)).into { (row, gId) =>
      row.copy(id = Some(gId))
    } += category

  override def getByCourse(courseId: Long) =
    questionCategories.filter(_.courseId === courseId).result

  override def getByCategory(categoryId: Option[Long], courseId: Long) =
    questionCategories.filter(cat => optionFilter(cat.parentId, categoryId) && cat.courseId === courseId).result

  override def getCountByCourse(courseId: Long) =
    questionCategories.filter(_.courseId === courseId).length.result

  override def getCountByCategory(categoryId: Option[Long], courseId: Long) =
    questionCategories.filter(cat => optionFilter(cat.parentId, categoryId) && cat.courseId === courseId).length.result

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long) = {
    val query = for {q <- questionCategories if q.id === id} yield (q.parentId, q.courseId)
    query.update(newCategoryId, courseId)
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean) = {
    if (moveToRoot) {
      val query = for {q <- questionCategories if q.id === id} yield (q.courseId, q.parentId)
      query.update(courseId, None)
    } else {
      val query = for {q <- questionCategories if q.id === id} yield q.courseId
      query.update(courseId)
    }
  }

}
