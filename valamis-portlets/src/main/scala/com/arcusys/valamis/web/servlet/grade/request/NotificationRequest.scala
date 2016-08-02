package com.arcusys.valamis.web.servlet.grade.request

import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequestModel, BaseRequest, Parameter}
import org.scalatra.ScalatraBase

object NotificationRequest extends BaseRequest{
  val TargetId = "targetId"
  val PackageTitle = "packageTitle"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {

    def targetId = Parameter(TargetId).longRequired
    def packageTitle = Parameter(PackageTitle).required
    def courseId = Parameter(CourseId).longRequired
  }
}
