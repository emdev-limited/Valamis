package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model.Category
import slick.dbio.DBIO

trait CategoryStorage extends ContentStorageBase[Category]{

  def getById(id:Long): DBIO[Option[Category]]

  def getByTitle(name: String): DBIO[Option[Category]]

  def getByTitleAndCourseId(name: String, courseId: Long): DBIO[Option[Category]]

  def create(category: Category): DBIO[Category]

  def update(category: Category): DBIO[Int]

  def delete(id: Long): DBIO[Int]

}
