package com.arcusys.valamis.liferay

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.ContentTypesHelper
import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.IndexerRegistryUtilHelper

class AssetHelper[T: Manifest] {

  protected val className = manifest[T].runtimeClass.getName

  def getEntry(classPK: Long): Option[LAssetEntry] = {
    AssetEntryLocalServiceHelper.fetchAssetEntry(className, classPK)
  }

  def deleteAssetEntry(classPK: Long): Unit = {
    for (entry <- AssetEntryLocalServiceHelper.fetchAssetEntry(className, classPK)) {
      deleteIndex(entry)
      AssetEntryLocalServiceHelper.deleteAssetEntry(entry.getEntryId)
    }
  }

  protected def updateAssetEntry(classPK: Long,
                                 userId: Option[Long],
                                 groupId: Option[Long],
                                 title: Option[String],
                                 description: Option[String],
                                 obj: T,
                                 companyId: Option[Long] = None,
                                 isVisible: Boolean = true): Long = {

    val assetEntry = getEntry(classPK) getOrElse {
      AssetEntryLocalServiceHelper.createAssetEntry(CounterLocalServiceHelper.increment)
    }

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
      assetEntry.setUserName(UserLocalServiceHelper().getUser(v).getFullName)
    })
    assetEntry.setMimeType(ContentTypesHelper.TEXT_HTML)
    assetEntry.setVisible(isVisible)

    AssetEntryLocalServiceHelper.updateAssetEntry(assetEntry)

    if (isVisible)
      reindex(obj)
    else
      deleteIndex(obj)

    assetEntry.getPrimaryKey
  }

  private def reindex(obj: T) = {
    for (indexer <- Option(IndexerRegistryUtilHelper.getIndexer[T](className)))
      indexer.reindex(obj)
  }

  private def deleteIndex(entry: LAssetEntry) = {
    for (indexer <- Option(IndexerRegistryUtilHelper.getIndexer[LAssetEntry](entry.getClassName)))
      indexer.delete(entry)
  }

  private def deleteIndex(obj: T) = {
    for (indexer <- Option(IndexerRegistryUtilHelper.getIndexer[T](className)))
      indexer.delete(obj)
  }
}
