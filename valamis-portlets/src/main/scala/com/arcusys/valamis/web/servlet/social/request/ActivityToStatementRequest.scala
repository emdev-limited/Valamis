package com.arcusys.valamis.web.servlet.social.request

import com.arcusys.valamis.web.servlet.request.{BaseRequest, Parameter}
import org.scalatra.ScalatraBase

object ActivityToStatementRequest extends BaseRequest {
  val ActivityClassName = "activityClassName"
  val Verb = "verb"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(scalatra: ScalatraBase) {
    implicit val _scalatra = scalatra

    def courseId = Parameter(CourseId).longRequired

    def activityClassName = Parameter(ActivityClassName).required

    def verb = Parameter(Verb).option("undefined")
  }

}
