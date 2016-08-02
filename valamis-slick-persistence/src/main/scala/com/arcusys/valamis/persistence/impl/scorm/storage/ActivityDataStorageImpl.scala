package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.ActivityDataMap
import com.arcusys.valamis.lesson.scorm.storage.ActivityDataStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityDataMapModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ActivityDataMapTableComponent

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class ActivityDataStorageImpl(val db: JdbcBackend#DatabaseDef,
                              val driver: JdbcProfile)
  extends ActivityDataStorage
    with ActivityDataMapTableComponent
    with SlickProfile {

  import driver.simple._

  override def create(packageId: Long, activityId: String, entity: ActivityDataMap): Unit =
    db.withSession { implicit session =>
      var activityDataMap = new ActivityDataMapModel(None,
        Some(packageId),
        Some(activityId),
        entity.targetId,
        entity.readSharedData,
        entity.writeSharedData)
       (activityDataMapTQ returning activityDataMapTQ.map(_.id)) += activityDataMap
    }


  override def delete(packageId: Long, activityId: String): Unit = db.withSession { implicit session =>
    activityDataMapTQ.filter(a => a.packageId === packageId && a.activityId === activityId).delete
  }

  override def getForActivity(packageId: Long, activityId: String): Seq[ActivityDataMap] =
    db.withSession { implicit session =>
      val activityDataMaps = activityDataMapTQ.filter(a => a.packageId === packageId && a.activityId === activityId).run
      activityDataMaps.map(a => new ActivityDataMap(a.targetId, a.readSharedData, a.writeSharedData))
    }

}
