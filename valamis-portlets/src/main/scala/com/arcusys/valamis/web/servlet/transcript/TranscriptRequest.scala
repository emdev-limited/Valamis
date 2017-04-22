package com.arcusys.valamis.web.servlet.transcript

import com.arcusys.valamis.certificate.AssignmentSort
import com.arcusys.valamis.certificate.model.AssignmentSortBy
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.user.model.UserSortBy
import com.arcusys.valamis.web.servlet.request.{BaseCollectionRequest, BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.joda.time.DateTime
import org.scalatra.ScalatraBase

object TranscriptRequest extends BaseCollectionRequest with BaseRequest {
  val UserId = "userId"
  val RequestCourseId = "courseId"
  val GroupId = "groupId"
  val CertificateId = "certificateId"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, UserSortBy.apply) {

    def requestedUserId = Parameter(UserId).longRequired

    def requestedCourseId = Parameter(RequestCourseId).longRequired

    def groupId = Parameter(GroupId).longOption(-1)

    def certificateId = Parameter(CertificateId).longRequired
  }
}
