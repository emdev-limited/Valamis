package com.arcusys.valamis.util

import java.io.Serializable

/**
  * Created by mminin on 19.10.15.
  */
trait CacheUtil {

  def get[T <: Serializable](key: String): Option[T]

  def put[T <: Serializable](key: String, value: T): Unit

  def getOr[T <: Serializable](key: String,
                               default: => Option[T]): Option[T]

  def clean[T <: Serializable](key: String): Unit

  def clean(): Unit
}

