package com.arcusys.valamis.web.servlet.social.request

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequestModel, BaseRequest, Parameter}
import com.arcusys.valamis.web.servlet.social.response.Activities
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.scalatra.ScalatraServlet

object ActivityRequest extends BaseRequest {
  val CompanyId = "companyId"
  val Content = "content"
  val Comment = "comment"
  val PackageId = "packageId"
  val GetMyActivities = "getMyActivities"
  val Id = "id"
  val ActivityName = "activity"
  val PlId = "plid"

  implicit val serializationFormats = DefaultFormats + new EnumNameSerializer(Activities) ++ org.json4s.ext.JodaTimeSerializers.all

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(val controller: ScalatraServlet) extends BaseCollectionFilteredRequestModel(controller) {
    implicit val scalatra = controller

    def action = Parameter(Action).required
    def companyIdServer = PortalUtilHelper.getCompanyId(controller.request)
    def content = Parameter(Content).required
    def comment = Parameter(Comment).option
    def packageId = Parameter(PackageId).longRequired
    def userIdServer = PermissionUtil.getUserId
    def getMyActivities = Parameter(GetMyActivities).booleanOption.getOrElse(false)
    def id = Parameter(Id).longRequired
    def activityName = Parameter(ActivityName).required
    def plId = Parameter(PlId).longRequired
  }
}
