package com.arcusys.learn.liferay.cache

import com.liferay.portal.kernel.cache.{MultiVMPoolUtil, PortalCache}

object MultiVMPoolHelper {
  def getCache[K <: java.io.Serializable, V <: java.io.Serializable](cacheName: String): PortalCache[K, V] = {
    MultiVMPoolUtil.getCache[K, V](cacheName)
  }

  def getCache[K <: java.io.Serializable, V <: java.io.Serializable](cacheName: String, blocking: Boolean): PortalCache[K, V] = {
    MultiVMPoolUtil.getCache[K, V](cacheName, blocking)
  }

  def removeCache(cacheName: String): Unit = {
    MultiVMPoolUtil.removeCache(cacheName)
  }
}
