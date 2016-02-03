package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model.Category

trait CategoryStorage extends ContentStorageBase[Category]{

  def getById(id:Long): Option[Category]

  def getByTitle(name: String): Option[Category]

  def getByTitleAndCourseId(name: String, courseId: Long): Option[Category]

  def create(category: Category): Category

  def update(category: Category): Unit

  def delete(id: Long): Unit


}
