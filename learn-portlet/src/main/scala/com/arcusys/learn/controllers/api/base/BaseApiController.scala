package com.arcusys.learn.controllers.api.base

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.controllers.auth.LiferayAuthSupport
import com.arcusys.learn.exceptions._
import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.utils.HTTPMethodsSupport
import com.arcusys.learn.view.extensions.i18nSupport
import com.arcusys.valamis.exception.{EntityDuplicateException, EntityNotFoundException}
import com.arcusys.valamis.lesson.exception.PassingLimitExceededException
import com.arcusys.valamis.util.LogSupport
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.NoSuchUserException
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.apache.http.client.RedirectException
import org.json4s.jackson.Serialization
import org.json4s.{Formats, NoTypeHints}
import org.scalatra.{RailsPathPatternParser, ScalatraServlet}

abstract class BaseApiController
  extends ScalatraServlet
  with HTTPMethodsSupport
  with Injectable
  with i18nSupport
  with LiferayAuthSupport
  with CSRFTokenSupport
  with LogSupport {

  implicit val bindingModule: BindingModule = Configuration
  implicit override def string2RouteMatcher(path: String) = RailsPathPatternParser(path)

  def jsonAction(a: => Any)(implicit formats: Formats = Serialization.formats(NoTypeHints)): Any = {
    val userAgent = request.getHeader("User-Agent")
    if (userAgent != null && (userAgent.contains("MSIE 9") || userAgent.contains("MSIE 8"))) //Because IE with versions below 10 doesn't support application/json
      response.setHeader("Content-Type", "text/html; charset=UTF-8")
    else response.setHeader("Content-Type", "application/json; charset=UTF-8")
    action {
      val result = a

      if (result != null && !result.isInstanceOf[Unit])
        return JsonHelper.toJson(result)
      else
        halt(HttpServletResponse.SC_NO_CONTENT)
    }
  }


  def action(action: => Any): Any = {
    try {
      action

    } catch {

      case e: NotAuthorizedException          => halt(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage)
      case e: AccessDeniedException           => halt(HttpServletResponse.SC_FORBIDDEN, e.getMessage)
      case e: BadRequestException             => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
      case e: EntityNotFoundException         => halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage)

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

  after() {
    response.setHeader("Cache-control", "must-revalidate,no-cache,no-store")
    response.setHeader("Expires", "-1")
  }

}
