package com.arcusys.valamis.web.servlet.liferay.request

import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequest, BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.scalatra.ScalatraBase

object LiferayRequest extends BaseCollectionFilteredRequest with BaseRequest {
  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, s => s) {
    implicit val httpRequest = scalatra.request

    def courseId = Parameter(CourseId).intRequired
  }
}
