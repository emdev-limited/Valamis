package com.arcusys.valamis.lesson.scorm.storage.tracking

trait DataModelStorage {
  def getKeyedValues(attemptId: Long, activityID: String): Map[String, Option[String]]
  def getValuesByKey(attemptId: Long, key: String): Map[String, Option[String]]
  def getValue(attemptID: Long, activityId: String, key: String): Option[String]
  def getCollectionValues(attemptId: Long, activityID: String, key: String): Map[String, Option[String]]
  def setValue(attemptID: Long, activityId: String, key: String, value: String)

}
