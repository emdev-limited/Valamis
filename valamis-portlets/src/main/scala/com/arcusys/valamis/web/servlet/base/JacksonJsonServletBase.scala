package com.arcusys.valamis.web.servlet.base

import javax.servlet.http.HttpServletResponse

import org.json4s.JsonAST.JString
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._

trait JacksonJsonServletBase extends JacksonJsonSupport {
  self: ScalatraServlet =>
  protected implicit val jsonFormats: Formats = DefaultFormats

  override def halt[T: Manifest](status: Integer = null,
                                 body: T = (),
                                 headers: Map[String, String] = Map.empty,
                                 reason: String = null) = {
    val newBody = body match {
      case s: String => JString(s)
      case _ => body
    }
    org.scalatra.halt(status, newBody, headers, reason)
  }

  before() {
    contentType = formats("json")
  }

  after() {
    response.setHeader("Cache-control", "must-revalidate,no-cache,no-store")
    response.setHeader("Expires", "-1")
    response.addHeader("Access-Control-Allow-Origin", "*")
  }

  options() {
    response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,HEAD,DELETE")
    response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length,Authorization,If-Match,If-None-Match,X-Experience-API-Version,X-Experience-API-Consistent-Through,X-Requested-With")
    response.setHeader("Access-Control-Expose-Headers", "ETag,Last-Modified,Cache-Control,Content-Type,Content-Length,WWW-Authenticate,X-Experience-API-Version,X-Experience-API-Consistent-Through")
  }

  // prevent: response 200 without content
  override protected def renderResponse(actionResult: Any) {
    super.renderResponse(actionResult match {
      case ActionResult(ResponseStatus(HttpServletResponse.SC_OK, _), null, headers) =>
        ActionResult(ResponseStatus(HttpServletResponse.SC_NO_CONTENT), null, headers)
      case () if status == HttpServletResponse.SC_OK =>
        status = HttpServletResponse.SC_NO_CONTENT
      case _ =>
        actionResult
    })
  }
}