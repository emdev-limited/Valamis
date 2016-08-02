package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.{AssetResource, Resource, ScoResource}
import com.arcusys.valamis.lesson.scorm.storage.ResourcesStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ResourceModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ResourceTableComponent

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class ResourcesStorageImpl(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends ResourcesStorage
  with ResourceTableComponent
    with SlickProfile {

  import driver.simple._

  override def getAll: Seq[Resource] = db.withSession { implicit s =>
    val resources = resourceTQ.run
    resources.map(_.convert)
  }

  override def getByID(packageId: Long, resourceId: String): Option[Resource] =
    db.withSession { implicit s =>
      val resources = resourceTQ.filter(r => r.packageId === packageId && r.resourceId === resourceId).firstOption
      resources.map(_.convert)
    }

  override def delete(packageId: Long): Unit = db.withSession { implicit s =>
    resourceTQ.filter(_.packageId === packageId).delete
  }

  override def getByPackageId(packageId: Long): Seq[Resource] =
    db.withSession { implicit s =>
      val resources = resourceTQ.filter(_.packageId === packageId).run
      resources.map(_.convert)
    }

  override def createForPackageAndGetId(packageId: Long, entity: Resource): Long =
    db.withSession { implicit s =>
      val scormType = entity match {
        case s: ScoResource => "sco"
        case a: AssetResource => "asset"
        case _ => throw new UnsupportedOperationException("Unknown resource type")
      }

      val resource = new ResourceModel(None,
        Some(packageId),
        scormType,
        Some(entity.id),
        entity.href,
        entity.base)

      val resourceId = (resourceTQ returning resourceTQ.map(_.id)) += resource
      resourceId
    }

  implicit class ResourceToModel(entity: ResourceModel) {
    def convert: Resource = {
      if (entity.scormType.equalsIgnoreCase("sco"))
        new ScoResource(entity.resourceId.get, entity.href.get, entity.base, Nil, Nil)
      else
        new AssetResource(entity.resourceId.get, entity.href, entity.base, Nil, Nil)
    }
  }
}
