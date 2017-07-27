package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.slide.service.contentProvider.model.ContentProvider

/**
  * Created By:
  * User: zsoltberki
  * Date: 27.9.2016
  */
trait ContentProviderRepository {
  def getAll(skipTake: Option[SkipTake],
             namePattern: Option[String],
             sortAscDirection: Boolean,
             companyId: Long): Seq[ContentProvider]
  def create(contentProvider: ContentProvider): ContentProvider
  def update(contentProvider: ContentProvider): ContentProvider
  def delete(id: Long): Unit
}
