package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.facades.UserFacadeContract
import com.arcusys.learn.policies.api.OrganizationPolicy

class OrganizationApiController extends BaseJsonApiController with OrganizationPolicy {

  private lazy val userFacade = inject[UserFacadeContract]

  get("/organizations(/)"){
    userFacade.getOrganizations
  }
}
