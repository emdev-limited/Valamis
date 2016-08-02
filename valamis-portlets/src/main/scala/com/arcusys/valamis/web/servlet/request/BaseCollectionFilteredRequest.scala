package com.arcusys.valamis.web.servlet.request

import org.scalatra.ScalatraBase

/**
 * Created by Iliya Tryapitsin on 12.03.14.
 */
object BaseCollectionFilteredRequest extends BaseCollectionFilteredRequest

trait BaseCollectionFilteredRequest extends BaseCollectionRequest {
  final val Filter = "filter"
}

abstract class BaseSortableCollectionFilteredRequestModel[T](scalatra: ScalatraBase, toEnum: String => T) extends BaseSortableCollectionRequestModel(scalatra, toEnum) {
  def filter = Parameter(BaseCollectionFilteredRequest.Filter).withDefault("")
  def textFilter = Parameter(BaseCollectionFilteredRequest.Filter).option("")
}

abstract class BaseCollectionFilteredRequestModel(scalatra: ScalatraBase) extends BaseCollectionRequestModel(scalatra) {
  def filter = Parameter(BaseCollectionFilteredRequest.Filter).withDefault("")
  def textFilter = Parameter(BaseCollectionFilteredRequest.Filter).option("")
}