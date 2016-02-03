package com.arcusys.learn.models.request

import com.arcusys.learn.service.util.Parameter
import org.scalatra.ScalatraBase

object LiferayRequest extends BaseCollectionFilteredRequest with BaseRequest {
  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, s => s) {
    implicit val httpRequest = scalatra.request

    def courseId = Parameter(CourseId).intRequired
  }
}
