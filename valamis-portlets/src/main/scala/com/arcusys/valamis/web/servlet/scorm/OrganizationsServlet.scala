package com.arcusys.valamis.web.servlet.scorm

import com.arcusys.valamis.lesson.scorm.model.manifest.Organization
import com.arcusys.valamis.lesson.scorm.service.ActivityServiceContract
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.BaseApiController
import com.arcusys.valamis.web.servlet.request.Parameter

class OrganizationsServlet extends BaseApiController {

  val activityManager = inject[ActivityServiceContract]

  private def transform(organization: Organization) = Map("id" -> organization.id, "title" -> organization.title)

  before() {
    response.setHeader("Cache-control", "must-revalidate,no-cache,no-store")
    response.setHeader("Expires", "-1")
  }

  get("/package/:packageID") {
    val packageID = Parameter("packageID")(this).intRequired
    JsonHelper.toJson(activityManager.getAllOrganizations(packageID).map(transform))
  }
}