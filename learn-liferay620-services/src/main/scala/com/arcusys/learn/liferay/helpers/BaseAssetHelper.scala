package com.arcusys.learn.liferay.helpers

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.util.IndexerRegistryUtilHelper

class BaseAssetHelper[T: Manifest] {

  protected val className = manifest[T].runtimeClass.getName

  protected def reindex(obj: T) = {
    for (indexer <- Option(IndexerRegistryUtilHelper.getIndexer(className)))
      indexer.reindex(obj)
  }

  protected def deleteIndex(entry: LAssetEntry) = {
    for (indexer <- Option(IndexerRegistryUtilHelper.getIndexer(entry.getClassName)))
      indexer.delete(entry)
  }

  protected def deleteIndex(obj: T) = {
    for (indexer <- Option(IndexerRegistryUtilHelper.getIndexer(className)))
      indexer.delete(obj)
  }
}
