package com.arcusys.valamis.util

import java.io.Serializable

import com.arcusys.learn.liferay.cache.MultiVMPoolHelper

/**
  * Created by mminin on 19.10.15.
  */
class CacheUtilMultiVMPoolImpl extends CacheUtil {
  private val cacheName = "valamis"

  def getOr[T <: Serializable](key: String,
                               default: => Option[T]): Option[T] = {
    get(key) orElse synchronized {
      get(key) orElse {
        default.foreach(v => put(key, v))
        default
      }
    }
  }

  def get[T <: Serializable](key: String): Option[T] = {
    val portalCache = MultiVMPoolHelper.getCache[String, T](cacheName)

    Option(portalCache.get(key))
      .filter(_.isInstanceOf[T])
      .map(_.asInstanceOf[T])
  }

  def put[T <: Serializable](key: String, value: T): Unit = {
    val portalCache = MultiVMPoolHelper.getCache[String, T](cacheName)

    portalCache.put(key, value)
  }

  def clean[T <: Serializable](key: String): Unit = {
    val portalCache = MultiVMPoolHelper.getCache[String, T](cacheName)
    portalCache.remove(key)
  }

  def clean(): Unit = {
    MultiVMPoolHelper.removeCache(cacheName)
  }
}
