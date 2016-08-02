package com.arcusys.valamis.content.storage

import slick.dbio.DBIO


trait ContentStorageBase[T/* <: Content*/] {
  def getByCourse(courseId: Long): DBIO[Seq[T]]
  def getCountByCourse(courseId: Long): DBIO[Int]
  def getByCategory(categoryId: Option[Long], courseId: Long): DBIO[Seq[T]]
  def getCountByCategory(categoryId: Option[Long], courseId: Long): DBIO[Int]

  def moveToCategory(id:Long, newCategoryId:Option[Long],courseId:Long): DBIO[Int]
  def moveToCourse(id:Long, courseId:Long,moveToRoot:Boolean): DBIO[Int]
}
