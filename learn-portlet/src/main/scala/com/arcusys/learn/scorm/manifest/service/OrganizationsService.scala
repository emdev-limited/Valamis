package com.arcusys.learn.scorm.manifest.service

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.web.ServletBase
import com.arcusys.valamis.lesson.scorm.model.manifest.Organization
import com.arcusys.valamis.lesson.service.ActivityServiceContract

class OrganizationsService extends BaseApiController with ServletBase {

  val activityManager = inject[ActivityServiceContract]

  val jsonModel = new JsonModelBuilder[Organization](organization => Map("id" -> organization.id, "title" -> organization.title))

  before() {
    response.setHeader("Cache-control", "must-revalidate,no-cache,no-store")
    response.setHeader("Expires", "-1")
  }

  get("/package/:packageID") {
    val packageID = parameter("packageID").intRequired
    jsonModel(activityManager.getAllOrganizations(packageID))
  }
}