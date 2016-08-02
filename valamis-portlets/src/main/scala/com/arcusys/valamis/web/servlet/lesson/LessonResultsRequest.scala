package com.arcusys.valamis.web.servlet.lesson

import com.arcusys.valamis.user.model.UserSortBy
import com.arcusys.valamis.web.servlet.request.{BaseCollectionRequest, BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.scalatra.ScalatraBase

object LessonResultsRequest extends BaseCollectionRequest with BaseRequest {
  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase)
    extends BaseSortableCollectionFilteredRequestModel(scalatra, UserSortBy.apply) {
    def courseId: Long = Parameter("courseId").longRequired
    def userId: Long = Parameter("userId").longRequired
    def organizationId: Option[Long] = Parameter("orgId").longOption
  }
}
