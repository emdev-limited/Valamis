package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.storage.tracking.DataModelStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.AttemptDataModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptDataTableComponent, AttemptTableComponent, ScormUserComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class DataModelStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends DataModelStorage
  with AttemptDataTableComponent
  with AttemptTableComponent
  with ScormUserComponent
  with SlickProfile {

  import driver.simple._

  override def getKeyedValues(attemptId: Long, activityId: String): Map[String, Option[String]] =
    db.withSession { implicit s =>
      val attemptData = attemptDataTQ.filter(a => a.activityId === activityId && a.attemptId === attemptId)
        .map(d => (d.dataKey, d.dataValue)).run
        attemptData.toMap
    }

  override def getValue(attemptId: Long, activityId: String, key: String): Option[String] =
    db.withSession { implicit s =>
      val attemptData = attemptDataTQ.filter(a => a.attemptId === attemptId &&
        a.activityId === activityId && a.dataKey === key)
        .map(_.dataValue).firstOption

      attemptData.flatten
    }

  override def setValue(attemptId: Long, activityId: String, key: String, value: String): Unit =
    db.withSession { implicit s =>
      getValue(attemptId, activityId, key) match {
        case None =>
          val attemptData = new AttemptDataModel(None, key, Some(value), attemptId, activityId)
          attemptDataTQ.insert(attemptData)
        case Some(_) =>
          val found = attemptDataTQ.filter(a => a.attemptId === attemptId &&
            a.activityId === activityId && a.dataKey === key).firstOption
          if (found.isDefined) {
            val entity = found.get.copy(dataValue = Some(value))
            attemptDataTQ.filter(_.id === entity.id.get).map(_.update).update(entity)

          }
      }
    }

  override def getCollectionValues(attemptId: Long, activityId: String, key: String): Map[String, Option[String]] =
    db.withSession { implicit s =>
      attemptDataTQ.filter(a => a.attemptId === attemptId && a.activityId === activityId && a.dataKey.like(key+"%"))
      .map(d => (d.dataKey, d.dataValue)).run.toMap
    }

  override def getValuesByKey(attemptId: Long, key: String): Map[String, Option[String]] =
    db.withSession { implicit s =>
      attemptDataTQ.filter(a => a.attemptId === attemptId && a.dataKey === key)
        .map(d => (d.activityId, d.dataValue)).run
        .toMap
    }
}
