package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.{LAssetEntry, LUpgradeProcess}
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil

import scala.collection.JavaConverters.asScalaBufferConverter

// convert package asset to lesson asset
class DBUpdater2716 extends LUpgradeProcess {

  override def getThreshold = 2716

  val newAssetClassName = "com.arcusys.valamis.lesson.model.Lesson"
  val oldAssetClassNames = Set(
    "com.arcusys.valamis.lesson.tincan.model.TincanPackage",
    "com.arcusys.valamis.lesson.tincan.model.TincanManifest",
    "com.arcusys.valamis.lesson.scorm.model.manifest.Manifest",
    "com.arcusys.valamis.lesson.model.BaseManifest"
  )

  override def doUpgrade(): Unit = {
    val newClassNameId = PortalUtilHelper.getClassNameId(newAssetClassName)

    oldAssetClassNames.toStream
      .map {
        PortalUtilHelper.getClassNameId
      }
      .foreach { classNameId =>
        updateClassNameId(classNameId, newClassNameId)
      }
  }


  private def updateClassNameId(classNameId: Long, newClassNameId: Long) = {
    val query = AssetEntryLocalServiceHelper.dynamicQuery()
      .add(RestrictionsFactoryUtil.eq("classNameId", classNameId))

    val assets = AssetEntryLocalServiceHelper.dynamicQuery(query)

    for (assetEntry <- assets) {
      assetEntry.setClassNameId(newClassNameId)
      AssetEntryLocalServiceHelper.updateAssetEntry(assetEntry)
    }
  }
}
