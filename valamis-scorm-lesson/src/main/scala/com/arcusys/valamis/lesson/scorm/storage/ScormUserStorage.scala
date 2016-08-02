package com.arcusys.valamis.lesson.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.ScormUser

trait ScormUserStorage {
  def getAll: Seq[ScormUser]
  def getById(userId: Long): Option[ScormUser]
  def getByName(name: String): Seq[ScormUser]
  def add(user: ScormUser): Long
  def modify(user: ScormUser)
  def delete(userId: Long)
  def getUsersWithAttempts: Seq[ScormUser]
}
