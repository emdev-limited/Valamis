package com.arcusys.valamis.course.storage

import com.arcusys.valamis.course.model.CourseExtended


trait CourseExtendedRepository {

  def getById(id: Long): Option[CourseExtended]

  def delete(id: Long): Unit

  def create(courseExtended: CourseExtended): CourseExtended

  def update(courseExtended: CourseExtended): CourseExtended

  def isExist(id: Long): Boolean
}