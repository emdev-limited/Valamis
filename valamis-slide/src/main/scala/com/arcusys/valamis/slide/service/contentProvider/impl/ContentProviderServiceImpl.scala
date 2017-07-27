package com.arcusys.valamis.slide.service.contentProvider.impl

import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.slide.service.contentProvider.ContentProviderService
import com.arcusys.valamis.slide.service.contentProvider.model.ContentProvider
import com.arcusys.valamis.slide.storage.ContentProviderRepository

/**
  * Created by ematyuhin on 13.12.16.
  */
abstract class ContentProviderServiceImpl extends ContentProviderService {

  def contentProviderRepository: ContentProviderRepository

  override def getAll(skipTake: Option[SkipTake],
                      namePattern: Option[String],
                      sortAscDirection: Boolean,
                      companyId: Long): RangeResult[ContentProvider] = {

    val providers = contentProviderRepository.getAll(skipTake, namePattern, sortAscDirection, companyId)
    val total = providers.size

    RangeResult(total, providers)
  }

  override def update(contentProvider: ContentProvider): ContentProvider = {
    contentProviderRepository.update(contentProvider)
  }

  override def add(contentProvider: ContentProvider): ContentProvider = {
    contentProviderRepository.create(contentProvider)
  }

  override def delete(providerId: Long): Unit = contentProviderRepository.delete(providerId)
}
