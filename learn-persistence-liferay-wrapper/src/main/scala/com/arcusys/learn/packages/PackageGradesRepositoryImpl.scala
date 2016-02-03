package com.arcusys.learn.packages

import com.arcusys.learn.persistence.liferay.model.LFPackageGradeStorage
import com.arcusys.learn.persistence.liferay.service._
import com.arcusys.learn.persistence.liferay.service.persistence.LFPackageGradeStoragePK
import com.arcusys.valamis.grade.model.PackageGrade
import com.arcusys.valamis.grade.storage.PackageGradesStorage
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import scala.util.Try

//TODO: change grade column type from string to float
class PackageGradesRepositoryImpl extends PackageGradesStorage {
  override def get(userId: Long, packageId: Long): Option[PackageGrade] = {
    val primaryKey = new LFPackageGradeStoragePK(userId, packageId)
    Option(LFPackageGradeStorageLocalServiceUtil.fetchLFPackageGradeStorage(primaryKey)).map(export)
  }

  override def get(userId: Long, packageIds: Seq[Long]): Seq[PackageGrade] = {
    if(packageIds.isEmpty)
      return Seq()

    val query = LFPackageGradeStorageLocalServiceUtil.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq("primaryKey.userId", userId))
      .add(RestrictionsFactoryUtil.in("primaryKey.packageId", packageIds.asJava))

    LFPackageGradeStorageLocalServiceUtil.dynamicQuery(query).asScala
      .map(grade => export(grade.asInstanceOf[LFPackageGradeStorage]))
  }

  override def delete(userId: Long, packageId: Long): Unit = {
    val primaryKey = new LFPackageGradeStoragePK(userId, packageId)
    LFPackageGradeStorageLocalServiceUtil.deleteLFPackageGradeStorage(primaryKey)
  }

  override def modify(entity: PackageGrade): PackageGrade = {
    val storageEntity = LFPackageGradeStorageLocalServiceUtil.findGrade(entity.userId, entity.packageId)

    storageEntity.setComment(entity.comment)
    storageEntity.setGrade(entity.grade.map(_.toString).getOrElse(""))

    val updatedEntity = LFPackageGradeStorageLocalServiceUtil.updateLFPackageGradeStorage(storageEntity)

    export(updatedEntity)
  }

  override def create(entity: PackageGrade): PackageGrade = {
    val packageGradeStorage = LFPackageGradeStorageLocalServiceUtil
      .createLFPackageGradeStorage(new LFPackageGradeStoragePK(entity.userId, entity.packageId))

    packageGradeStorage.setComment(entity.comment)
    packageGradeStorage.setGrade(entity.grade.map(_.toString).getOrElse(""))
    packageGradeStorage.setDate(DateTime.now().toDate)

    val addedPackageGradeStorage = LFPackageGradeStorageLocalServiceUtil.addLFPackageGradeStorage(packageGradeStorage)
    export(addedPackageGradeStorage)
  }

  private def export(lfEntity: LFPackageGradeStorage) = {
    PackageGrade(
      lfEntity.getUserId,
      lfEntity.getPackageId,
      Try(lfEntity.getGrade.toFloat).toOption,
      lfEntity.getComment,
      Option(lfEntity.getDate).map(new DateTime(_))
    )
  }
}
