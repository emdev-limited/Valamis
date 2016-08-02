package com.arcusys.valamis.content.storage.impl

import com.arcusys.valamis.content.model.PlainText
import com.arcusys.valamis.persistence.common.{OptionFilterSupport3, SlickProfile}
import com.arcusys.valamis.content.storage.PlainTextStorage
import com.arcusys.valamis.content.storage.impl.schema.ContentTableComponent
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

/**
  * *  Created by pkornilov on 23.10.15.
  */

class PlainTextStorageImpl(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends PlainTextStorage
    with ContentTableComponent
    with OptionFilterSupport3
    with SlickProfile {

  import driver.api._

  override def getById(id: Long) =
    plainTexts.filter(_.id === id).result.headOption

  override def update(plainText: PlainText) =
    plainTexts.filter(_.id === plainText.id).map(_.update).update(plainText)

  override def delete(id: Long) =
    plainTexts.filter(_.id === id).delete

  override def create(plainText: PlainText) = {
    (plainTexts returning plainTexts.map(_.id)).into { (row, gId) =>
      row.copy(id = Some(gId))
    } += plainText
  }

  override def getByCourse(courseId: Long) =
    plainTexts.filter(_.courseId === courseId).result

  override def getByCategory(categoryId: Option[Long], courseId: Long) =
    plainTexts.filter(pt => optionFilter(pt.categoryId, categoryId) && pt.courseId === courseId).result

  override def getCountByCourse(courseId: Long) =
    plainTexts.filter(_.courseId === courseId).length.result

  override def getCountByCategory(categoryId: Option[Long], courseId: Long) =
    plainTexts.filter(pt => optionFilter(pt.categoryId, categoryId) && pt.courseId === courseId).length.result

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long) = {
    val query = for {q <- plainTexts if q.id === id} yield (q.categoryId, q.courseId)
    query.update(newCategoryId, courseId)
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean) = {
    if (moveToRoot) {
      val query = for {q <- plainTexts if q.id === id} yield (q.courseId, q.categoryId)
      query.update(courseId, None)
    } else {
      val query = for {q <- plainTexts if q.id === id} yield q.courseId
      query.update(courseId)
    }
  }

}
