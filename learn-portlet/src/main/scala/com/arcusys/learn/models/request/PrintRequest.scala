package com.arcusys.learn.models.request

import com.arcusys.learn.models.request.PrintActionType._
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequest, BaseCollectionFilteredRequestModel, BaseRequest, Parameter}
import org.scalatra.ScalatraBase

object PrintRequest extends BaseCollectionFilteredRequest with BaseRequest {
  val PrintTranscript = "PRINT_TRANSCRIPT"
  val CompanyId = "companyID"
  val UserId = "userID"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {

    def actionType: PrintActionType = PrintActionType.withName(Parameter(Action).required.toUpperCase)

    def companyId = Parameter(CompanyId).intRequired

    def userId = Parameter(UserId).intRequired

    def courseId = Parameter(CourseId).longRequired
  }

}

