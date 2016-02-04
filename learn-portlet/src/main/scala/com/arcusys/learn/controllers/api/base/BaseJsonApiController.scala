package com.arcusys.learn.controllers.api.base

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.controllers.auth.LiferayAuthSupport
import com.arcusys.learn.exceptions.{AccessDeniedException, BadRequestException, NotAuthorizedException}
import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.utils.HTTPMethodsSupport
import com.arcusys.learn.view.extensions.i18nSupport
import com.arcusys.valamis.exception.{EntityDuplicateException, EntityNotFoundException}
import com.arcusys.valamis.lesson.exception.PassingLimitExceededException
import com.arcusys.valamis.util.LogSupport
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.NoSuchUserException
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.apache.http.client.RedirectException
import org.json4s.JsonAST.JString
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import org.scalatra.servlet.SizeConstraintExceededException

abstract class BaseJsonApiController
  extends ScalatraServlet
  with Injectable
  with HTTPMethodsSupport
  with JacksonJsonSupport
  with i18nSupport
  with LiferayAuthSupport
  with CSRFTokenSupport
  with LogSupport {

  implicit lazy val bindingModule: BindingModule = Configuration
  implicit override def string2RouteMatcher(path: String) = RailsPathPatternParser(path)

  protected implicit val jsonFormats: Formats = DefaultFormats

  override def halt[T: Manifest](status: Integer = null,
    body: T = (),
    headers: Map[String, String] = Map.empty,
    reason: String = null) = {
    val newBody = body match {
      case s: String => JString(s)
      case _         => body
    }
    org.scalatra.halt(status, newBody, headers, reason)
  }

  def action(action: => Any): Any = {
    try {
      action

    } catch {

      case e: NotAuthorizedException          => halt(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage)
      case e: AccessDeniedException           => halt(HttpServletResponse.SC_FORBIDDEN, e.getMessage)
      case e: BadRequestException             => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
      case e: EntityNotFoundException         => halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
      //      case e: NoSuchElementException          => halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
      case e: EntityDuplicateException        => halt(HttpServletResponse.SC_CONFLICT, e.getMessage)
      case e: NoSuchUserException             => halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage, reason = "No user exists")
      case e: PassingLimitExceededException   => halt(HttpServletResponse.SC_FORBIDDEN)
      case e: ParameterNamesNotFoundException => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
      case e: RedirectException               => response.sendRedirect(e.getMessage)
      case e: Exception =>
        log.error(e)
        halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
    }
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
    response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length,Authorization,If-Match,If-None-Match,X-Experience-API-Version,X-Experience-API-Consistent-Through")
    response.setHeader("Access-Control-Expose-Headers", "ETag,Last-Modified,Cache-Control,Content-Type,Content-Length,WWW-Authenticate,X-Experience-API-Version,X-Experience-API-Consistent-Through")
  }

  error {
    case e: NotAuthorizedException          => halt(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage)
    case e: AccessDeniedException           => halt(HttpServletResponse.SC_FORBIDDEN, e.getMessage)
    case e: BadRequestException             => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
    case e: EntityNotFoundException         => halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
    case e: EntityDuplicateException        => halt(HttpServletResponse.SC_CONFLICT, e.getMessage)
    case e: NoSuchUserException             => halt(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage, reason = "No user exists")
    case e: PassingLimitExceededException   => halt(HttpServletResponse.SC_FORBIDDEN)
    case e: ParameterNamesNotFoundException => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
    case e: SizeConstraintExceededException => RequestEntityTooLarge("too much!")
    case e: Exception =>
      log.error(e)
      halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
  }

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