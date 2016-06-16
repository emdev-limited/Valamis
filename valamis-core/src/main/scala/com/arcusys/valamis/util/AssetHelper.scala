package com.arcusys.valamis.util

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.ContentTypesHelper
import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.IndexerRegistryUtilHelper
import com.liferay.portal.service.UserLocalServiceUtil

import scala.util.Try
import java.util.Date

class AssetHelper {

  def getEntry(className: String, classPK: Long) =
    Try(AssetEntryLocalServiceHelper.getAssetEntry(className, classPK)).toOption

  def deleteAssetEntry(className: String, classPK: Long) = {
    try {
      val entry = AssetEntryLocalServiceHelper.getAssetEntry(className, classPK)
      deleteIndex(entry)
      AssetEntryLocalServiceHelper.deleteAssetEntry(entry.getEntryId)
    } catch {
      case e: LNoSuchEntryException => System.out.println("Asset not found")
    }
  }

  def deleteAssetEntry(entryId: Long, obj: Object) {
    try {
      if (AssetEntryLocalServiceHelper.getAssetEntry(entryId) != null) {
        deleteIndex(obj)
        AssetEntryLocalServiceHelper.deleteAssetEntry(entryId)
      }
    } catch {
      case e: LNoSuchEntryException => System.out.println("Asset not found")
    }
  }

  protected def updateAssetEntry(entryId: Option[Long],
                                 classPK: Long,
                                 userId: Option[Long],
                                 groupId: Option[Long],
                                 title: Option[String],
                                 description: Option[String],
                                 obj: Object,
                                 companyId: Option[Long] = None,
                                 isVisible: Boolean = true): Long = {

    val assetEntry = entryId match {
      case Some(v) =>
        AssetEntryLocalServiceHelper.getAssetEntry(v)
      case None =>
        val entryId = CounterLocalServiceHelper.increment
        AssetEntryLocalServiceHelper.createAssetEntry(entryId)
    }

    val className = obj.getClass.getName
    assetEntry.setClassPK(classPK)
    assetEntry.setClassName(className)
    assetEntry.setClassNameId(ClassNameLocalServiceHelper.getClassNameId(className))
    title.foreach(assetEntry.setTitle)
    description.foreach(assetEntry.setSummary)
    companyId.foreach(v => {
      assetEntry.setCompanyId(v)
      assetEntry.setGroupId(CompanyLocalServiceHelper.getCompanyGroupId(v))
    })
    groupId.foreach(v => {
      assetEntry.setGroupId(v)
      assetEntry.setCompanyId(GroupLocalServiceHelper.getGroup(v).getCompanyId)
    })
    userId.foreach(v => {
      assetEntry.setUserId(v)
      assetEntry.setUserName(UserLocalServiceUtil.getUser(v).getFullName)
    })
    assetEntry.setMimeType(ContentTypesHelper.TEXT_HTML)
    assetEntry.setVisible(isVisible)
    
    //Set create and modified date (needs for indexer)
    if (assetEntry.isNew()){
      assetEntry.setCreateDate(new Date());
      assetEntry.setModifiedDate(new Date());
    } else {
      assetEntry.setModifiedDate(new Date());
    }
    
    AssetEntryLocalServiceHelper.updateAssetEntry(assetEntry)

    if (isVisible)
      reindex(obj)
    else
      deleteIndex(obj)

    assetEntry.getPrimaryKey
  }

  private def reindex(obj: Object) = {
    val indexer = IndexerRegistryUtilHelper.getIndexer(obj.getClass.getName)
    if (indexer != null) indexer.reindex(obj)
  }

  private def deleteIndex(entry: LAssetEntry) = {
    val indexer = IndexerRegistryUtilHelper.getIndexer(entry.getClassName)
    if (indexer != null) indexer.delete(entry)
  }

  private def deleteIndex(obj: Object) = {
    val indexer = IndexerRegistryUtilHelper.getIndexer(obj.getClass.getName)
    if (indexer != null) indexer.delete(obj)
  }
}
