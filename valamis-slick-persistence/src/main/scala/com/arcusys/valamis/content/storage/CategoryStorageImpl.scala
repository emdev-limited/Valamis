package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model.Category
import com.arcusys.valamis.content.schema.ContentTableComponent
import com.arcusys.valamis.core.OptionFilterSupport


import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 * *
 * Created by pkornilov on 23.10.15.
 */

class CategoryStorageImpl(val db:     JdbcBackend#DatabaseDef,
                          val driver: JdbcProfile)
  extends CategoryStorage with ContentTableComponent with OptionFilterSupport{

  import driver.simple._

  override def getByTitle(name: String): Option[Category] = db.withSession { implicit session =>
    questionCategories.filter(_.title === name).firstOption
  }

  override def getByTitleAndCourseId(name: String, courseId: Long): Option[Category] = db.withSession { implicit session =>
    questionCategories.filter( q => q.title === name && q.courseId === courseId).firstOption
  }

  override def getById(id: Long): Option[Category] = db.withSession { implicit session =>
    questionCategories.filter(_.id === id).firstOption
  }

  override def update(category: Category): Unit = db.withSession { implicit session =>
    questionCategories.filter(_.id === category.id).map(cat => (cat.title, 
                                                                cat.description,  
                                                                cat.courseId)).update((category.title, category.description, category.courseId))
  }

  override def delete(id: Long): Unit = db.withSession { implicit session =>
    questionCategories.filter(_.parentId === id).delete
    questionCategories.filter(_.id === id).delete

  }

  override def create(category: Category): Category = db.withTransaction { implicit session =>
    val id = (questionCategories returning questionCategories.map(_.id)).insert(category)
    questionCategories.filter(_.id === id).first
  }

  override def getByCourse(courseId: Long): Seq[Category] = db.withSession { implicit s =>
      questionCategories.filter(_.courseId === courseId).list
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Category] = db.withSession {implicit s=>
    questionCategories.filter(cat => optionFilter(cat.parentId,categoryId) && cat.courseId === courseId).list
  }

  override def getCountByCourse(courseId: Long): Int = db.withSession { implicit s =>
    questionCategories.filter(_.courseId === courseId).length.run
  }

  override def getCountByCategory(categoryId: Option[Long], courseId: Long): Int = db.withSession  { implicit s =>
    questionCategories.filter(cat => optionFilter(cat.parentId,categoryId) && cat.courseId === courseId).length.run
  }

  override def moveToCategory(id:Long, newCategoryId:Option[Long],courseId:Long) = db.withSession {implicit s =>
    val query = for { q <- questionCategories if q.id === id } yield (q.parentId,q.courseId)
    query.update(newCategoryId,courseId).run
  }

  override def moveToCourse(id:Long, courseId:Long,moveToRoot:Boolean) = db.withTransaction { implicit s =>
    if (moveToRoot) {
      val query = for {q <- questionCategories if q.id === id} yield (q.courseId,q.parentId)
      query.update(courseId,None).run
    } else {
      val query = for {q <- questionCategories if q.id === id} yield q.courseId
      query.update(courseId).run
    }
  }

}
