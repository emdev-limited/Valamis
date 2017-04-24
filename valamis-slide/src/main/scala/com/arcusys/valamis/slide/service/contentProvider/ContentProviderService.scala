package com.arcusys.valamis.slide.service.contentProvider

import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.slide.service.contentProvider.model.ContentProvider

/**
  * Created By:
  * User: zsoltberki
  * Date: 28.9.2016
  */
trait ContentProviderService {
  def getAll(skipTake: Option[SkipTake],
             namePattern: Option[String],
             sortAscDirection: Boolean): RangeResult[ContentProvider]

  def update(contentProvider: ContentProvider): ContentProvider

  def add(contentProvider: ContentProvider): ContentProvider


  def delete(providerId: Long): Unit
}


