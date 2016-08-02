package com.arcusys.valamis.web.servlet.lessonStudio

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients

import scala.util.Try

/**
  * Url checks
  */
class UrlServlet extends BaseApiController {

  before(request.getMethod == "POST") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonStudio)
  }

  post("/url/check(/)") {
    val url = params("url").trim

    if(url.nonEmpty) {
      val client = HttpClients.createDefault()

      Try {
        val httpResponse = client.execute(new HttpGet(url))
        // Check if headers set and if not, return true
        val headers = httpResponse.getHeaders("X-Frame-Options") ++ httpResponse.getHeaders("x-frame-options")

        httpResponse.close()
        client.close()

        headers
      }.toOption
        .exists(_.length == 0)
    } else false
  }
}
