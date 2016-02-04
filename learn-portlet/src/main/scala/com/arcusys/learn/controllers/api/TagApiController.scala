package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.models.TagResponse
import com.arcusys.learn.web.ServletBase
import com.arcusys.valamis.lesson.service.TagServiceContract

class TagApiController extends BaseApiController with ServletBase {

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  options() {
    response.setHeader("Access-Control-Allow-Methods", "HEAD,GET,POST,PUT,DELETE")
    response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length,Authorization,If-Match,If-None-Match,X-Experience-API-Version,X-Experience-API-Consistent-Through")
    response.setHeader("Access-Control-Expose-Headers", "ETag,Last-Modified,Cache-Control,Content-Type,Content-Length,WWW-Authenticate,X-Experience-API-Version,X-Experience-API-Consistent-Through")
  }

  private val tagService = inject[TagServiceContract]

  //List Action
  get("/tags(/)")(jsonAction {
    val companyId = getCompanyId.toInt
    tagService.getAll(companyId)
      .map(t => TagResponse(t.id, t.text))
  })
}
