package com.arcusys.valamis.web.servlet

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.web.servlet.base.BaseApiController
import org.scalatra.servlet.ServletBase

class TagServlet extends BaseApiController with ServletBase {

  options() {
    response.setHeader("Access-Control-Allow-Methods", "HEAD,GET,POST,PUT,DELETE")
    response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length,Authorization,If-Match,If-None-Match,X-Experience-API-Version,X-Experience-API-Consistent-Through")
    response.setHeader("Access-Control-Expose-Headers", "ETag,Last-Modified,Cache-Control,Content-Type,Content-Length,WWW-Authenticate,X-Experience-API-Version,X-Experience-API-Consistent-Through")
  }

  private lazy val tagService = inject[TagService[Lesson]]

  get("/tags(/)")(jsonAction {
    tagService.getAll(CompanyHelper.getCompanyId)
  })
}
