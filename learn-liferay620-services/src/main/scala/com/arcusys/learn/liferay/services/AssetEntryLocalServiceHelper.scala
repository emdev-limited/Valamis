package com.arcusys.learn.liferay.services

import com.liferay.portal.kernel.dao.orm.{DynamicQuery, RestrictionsFactoryUtil}
import com.liferay.portal.util.PortalUtil
import com.liferay.portlet.asset.model.AssetEntry
import com.liferay.portlet.asset.service.{AssetCategoryLocalServiceUtil, AssetEntryLocalServiceUtil}

import scala.collection.JavaConverters._

object AssetEntryLocalServiceHelper {
  def dynamicQuery(query: DynamicQuery): Seq[AssetEntry] = AssetEntryLocalServiceUtil.dynamicQuery(query).asScala.map(_.asInstanceOf[AssetEntry])

  def dynamicQuery(): DynamicQuery = AssetEntryLocalServiceUtil.dynamicQuery()

  def getAssetEntry(entryId: Long): AssetEntry = AssetEntryLocalServiceUtil.getEntry(entryId)

  def getAssetEntry(className: String, classPK: Long): AssetEntry = AssetEntryLocalServiceUtil.getEntry(className, classPK)

  def fetchAssetEntry(className: String, classPK: Long): Option[AssetEntry] = Option(AssetEntryLocalServiceUtil.fetchEntry(className, classPK))

  def fetchAssetEntry(entryId: Long): Option[AssetEntry] = Option(AssetEntryLocalServiceUtil.fetchEntry(entryId))

  def fetchAssetEntries(className: String, classPK: Seq[Long]): Seq[AssetEntry] = {
    val classNameId = PortalUtil.getClassNameId(className)
    classPK match {
      case Nil => Seq()
      case seq =>
        val ids = seq.asJavaCollection
        val query = AssetEntryLocalServiceUtil.dynamicQuery()
          .add(RestrictionsFactoryUtil.eq("classNameId", classNameId))
          .add(RestrictionsFactoryUtil.in("classPK", ids))

        AssetEntryLocalServiceUtil.dynamicQuery(query).asScala
          .map(_.asInstanceOf[AssetEntry])
    }
  }

  def getAssetEntriesByCategory(categoryId: Long, classNameId: Long): Seq[AssetEntry] = {
    AssetEntryLocalServiceUtil.getAssetCategoryAssetEntries(categoryId).asScala
      .filter(_.getClassNameId == classNameId)
  }

  def deleteAssetEntry(entryId: Long): AssetEntry = AssetEntryLocalServiceUtil.deleteAssetEntry(entryId)

  def createAssetEntry(entryId: Long): AssetEntry =
    AssetEntryLocalServiceUtil.createAssetEntry(entryId)

  def updateAssetEntry(assetEntry: AssetEntry): AssetEntry = {
    if (assetEntry.isNew) AssetEntryLocalServiceUtil.addAssetEntry(assetEntry)
    else AssetEntryLocalServiceUtil.updateAssetEntry(assetEntry)
  }

  def setAssetCategories(entryId: Long, categoryIds: Array[Long]): Unit = {
    if (!categoryIds.isEmpty)
      AssetCategoryLocalServiceUtil.setAssetEntryAssetCategories(entryId, categoryIds)
    else
      AssetCategoryLocalServiceUtil.clearAssetEntryAssetCategories(entryId)
  }

  def addAssetCategories(entryId: Long, categoryIds: Array[Long]): Unit =
    AssetCategoryLocalServiceUtil.addAssetEntryAssetCategories(entryId, categoryIds)

  def removeAssetCategories(entryId: Long, categoryIds: Array[Long]): Unit =
    AssetCategoryLocalServiceUtil.deleteAssetEntryAssetCategories(entryId, categoryIds)
}
