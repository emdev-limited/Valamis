package com.arcusys.valamis.slide.service

import com.arcusys.learn.liferay.LiferayClasses.LAssetEntry
import com.arcusys.learn.liferay.services.{AssetEntryLocalServiceHelper, CompanyHelper}
import com.arcusys.valamis.liferay.AssetHelper
import com.arcusys.valamis.slide.model.SlideSetModel
import com.arcusys.valamis.tag.model.ValamisTag

import scala.collection.JavaConverters._

trait SlideSetAssetHelper {
  def updateSlideAsset(slideSet: SlideSetModel, userId: Option[Long]): Long

  def getSlideAsset(slideSetId: Long): Option[LAssetEntry]

  def getSlideAssets(slideSetIds: Seq[Long]): Seq[LAssetEntry]

  def getSlideAssetCategories(slideSetId: Long): Seq[ValamisTag]

  def getSlidesAssetCategories(slideSetId: Seq[Long]): Seq[(Long, Seq[ValamisTag])]

  def deleteSlideAsset(slideSetId: Long): Unit
}

class SlideSetAssetHelperImpl extends AssetHelper[SlideSetModel] with SlideSetAssetHelper {

  private lazy val assetHelper = AssetEntryLocalServiceHelper

  def updateSlideAsset(slideSet: SlideSetModel, userId: Option[Long]) = {

    if (slideSet.id.isEmpty)
      throw new IllegalArgumentException("Slideset id is empty")

    updateAssetEntry(
      slideSet.id.get,
      userId,
      if (slideSet.courseId != -1L) Some(slideSet.courseId) else None,
      Some(slideSet.title),
      Some(slideSet.description),
      slideSet,
      Option(CompanyHelper.getCompanyId))
  }

  def getSlideAsset(slideSetId: Long) =
    assetHelper.fetchAssetEntry(className, slideSetId)

  def getSlideAssets(slideSetIds: Seq[Long]) =
    assetHelper.fetchAssetEntries(className, slideSetIds)

  def getSlideAssetCategories(slideSetId: Long) =
    getSlideAsset(slideSetId) map { asset =>
      asset.getCategories.asScala.map(c => ValamisTag(c.getCategoryId, c.getName)).toSeq
    } getOrElse Seq()

  def getSlidesAssetCategories(slideSetIds: Seq[Long]) =
    getSlideAssets(slideSetIds) map { asset =>
      val tags = asset.getCategories.asScala.map(c => ValamisTag(c.getCategoryId, c.getName)).toSeq
      (asset.getClassPK, tags)
    }

  def deleteSlideAsset(slideSetId: Long) = {
    getSlideAsset(slideSetId).map { asset =>
        assetHelper.deleteAssetEntry(asset.getEntryId)
      }
  }
}