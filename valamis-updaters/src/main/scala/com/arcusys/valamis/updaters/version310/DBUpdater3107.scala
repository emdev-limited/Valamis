package com.arcusys.valamis.updaters.version310

import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version310.slide.SlideTableComponent
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBUpdater3107 extends BaseDBUpdater with SlideTableComponent {

  override def getThreshold = 3107
  private val oldClassName = "com.arcusys.valamis.slide.model.SlideSetModel"
  private val newClassName = "com.arcusys.valamis.slide.model.SlideSet"

  import driver.api._

  override def doUpgrade(): Unit = {

    val action = slideSets.map(_.id).result
    val slideSetsIds = Await.result(dbInfo.databaseDef.run(action), Duration.Inf)

    AssetEntryLocalServiceHelper.fetchAssetEntries(oldClassName, slideSetsIds)
      .foreach { asset =>
        AssetEntryLocalServiceHelper.fetchAssetEntry(newClassName, asset.getClassPK) match {
          case Some(entry) =>
            val diff = asset.getCategoryIds.diff(entry.getCategoryIds)
            AssetEntryLocalServiceHelper.addAssetCategories(entry.getEntryId, diff)

          case None =>
            val assetEntry = AssetEntryLocalServiceHelper.createAssetEntry(CounterLocalServiceHelper.increment)
            assetEntry.setClassPK(asset.getClassPK)
            assetEntry.setClassNameId(PortalUtilHelper.getClassNameId(newClassName))
            assetEntry.setTitle(asset.getTitle)
            assetEntry.setSummary(asset.getSummary)
            assetEntry.setCompanyId(asset.getCompanyId)
            assetEntry.setGroupId(asset.getGroupId)
            assetEntry.setUserId(asset.getUserId)
            assetEntry.setMimeType(asset.getMimeType)
            assetEntry.setVisible(asset.getVisible)
            AssetEntryLocalServiceHelper.updateAssetEntry(assetEntry)

            AssetEntryLocalServiceHelper.addAssetCategories(assetEntry.getEntryId, asset.getCategoryIds)
        }
      }
  }

}
