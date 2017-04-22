package com.arcusys.valamis.web.servlet.base

import javax.servlet.http.HttpServletResponse

import com.arcusys.valamis.util.serialization.JsonHelper
import org.json4s.jackson.Serialization
import org.json4s.{Formats, NoTypeHints}

abstract class BaseApiController extends ServletBase with PermissionSupport {

  def jsonAction(a: => Any)(implicit formats: Formats = Serialization.formats(NoTypeHints)): Any = {
    val userAgent = request.getHeader("User-Agent")
    if (userAgent != null && (userAgent.contains("MSIE 9") || userAgent.contains("MSIE 8"))) //Because IE with versions below 10 doesn't support application/json
      response.setHeader("Content-Type", "text/html; charset=UTF-8")
    else response.setHeader("Content-Type", "application/json; charset=UTF-8")

    val result = a

    if (result != null && !result.isInstanceOf[Unit])
      JsonHelper.toJson(result)
    else
      halt(HttpServletResponse.SC_NO_CONTENT)
  }

  after() {
    response.setHeader("Cache-control", "must-revalidate,no-cache,no-store")
    response.setHeader("Expires", "-1")
  }

}
