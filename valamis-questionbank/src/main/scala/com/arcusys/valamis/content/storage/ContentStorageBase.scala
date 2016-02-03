package com.arcusys.valamis.content.storage


trait ContentStorageBase[T/* <: Content*/] {
  def getByCourse(courseId: Long): Seq[T]
  def getCountByCourse(courseId: Long): Int
  def getByCategory(categoryId: Option[Long], courseId: Long): Seq[T]
  def getCountByCategory(categoryId: Option[Long], courseId: Long): Int

  def moveToCategory(id:Long, newCategoryId:Option[Long],courseId:Long)
  def moveToCourse(id:Long, courseId:Long,moveToRoot:Boolean)
}
