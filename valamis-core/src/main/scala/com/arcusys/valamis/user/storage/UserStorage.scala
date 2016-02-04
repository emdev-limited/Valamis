package com.arcusys.valamis.user.storage

import com.arcusys.valamis.user.model.{ScormUser, User}

trait UserStorage {
  def getAll: Seq[ScormUser]
  def getByID(userId: Int): Option[ScormUser]
  def getByName(name: String): Seq[ScormUser]
  def createAndGetID(user: ScormUser): Int
  def modify(user: ScormUser)
  def delete(userId: Int)
  def getUsersWithAttempts: Seq[ScormUser]
  def renew()
}
