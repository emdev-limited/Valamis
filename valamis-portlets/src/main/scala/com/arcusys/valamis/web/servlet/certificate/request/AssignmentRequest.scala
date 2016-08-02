package com.arcusys.valamis.web.servlet.certificate.request

import com.arcusys.valamis.certificate.AssignmentSort
import com.arcusys.valamis.certificate.model.AssignmentSortBy
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.web.servlet.request._
import org.json4s.DefaultFormats
import org.scalatra.ScalatraBase

object AssignmentRequest extends BaseCollectionFilteredRequest with BaseRequest {
  val Id = "id"
  val CertificateId = "certificateId"
  val Title = "title"
  val Deadline = "deadline"
  val GroupId = "groupId"
  val Status = "status"
  val UserId = "userId"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  implicit val serializationFormats = DefaultFormats

  class Model(val scalatra: ScalatraBase)
    extends BaseSortableCollectionFilteredRequestModel(scalatra, AssignmentSortBy.apply) {

    implicit val httpRequest = scalatra.request

    def id = Parameter(Id).intRequired
    def idOption = Parameter(Id).longOption
    def certificateId = Parameter(CertificateId).intRequired
    def title = Parameter(Title).option("")
    def deadline = Parameter(Deadline).dateTimeOption("")
    def groupId = Parameter(GroupId).longOption
    def status = Parameter(Status).option("")
    def userId = Parameter(UserId).longRequired

    def sort = Parameter(BaseCollectionRequest.SortBy).option
      .map(o => AssignmentSort(AssignmentSortBy.apply(o), Order(ascending)))

  }
}