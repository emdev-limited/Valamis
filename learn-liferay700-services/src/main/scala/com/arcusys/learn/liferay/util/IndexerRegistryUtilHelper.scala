package com.arcusys.learn.liferay.util

import com.arcusys.learn.liferay.LiferayClasses.LIndexer
import com.liferay.portal.kernel.search.IndexerRegistryUtil

object IndexerRegistryUtilHelper {
  def getIndexer[T](clazz: Class[T]): LIndexer[T] = IndexerRegistryUtil.getIndexer[T](clazz)

  def getIndexer[T](className: String): LIndexer[T] = IndexerRegistryUtil.getIndexer[T](className)
}
