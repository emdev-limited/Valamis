package com.arcusys.valamis.lesson.scorm.storage.tracking

import com.arcusys.valamis.lesson.scorm.model.tracking.Attempt

trait AttemptStorage {
  def getActive(userId: Long, packageId: Long): Option[Attempt]
  def getAllComplete(userId: Long, packageId: Long): Seq[Attempt]
  def getByID(id: Long): Option[Attempt]
  def getLast(userId: Long, packageId: Long, complete: Boolean = false): Option[Attempt]
  def createAndGetID(userId: Long, packageId: Long, organizationID: String): Long
  def markAsComplete(id: Long)
  def checkIfComplete(userId: Long, packageId: Long): Boolean
}
