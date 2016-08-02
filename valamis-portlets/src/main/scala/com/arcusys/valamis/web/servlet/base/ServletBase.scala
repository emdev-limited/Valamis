package com.arcusys.valamis.web.servlet.base

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.exception.{EntityDuplicateException, EntityNotFoundException}
import com.arcusys.valamis.lesson.exception.PassingLimitExceededException
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.web.configuration.InjectableSupport
import com.arcusys.valamis.web.servlet.base.auth.LiferayAuthSupport
import com.arcusys.valamis.web.servlet.base.exceptions.{AccessDeniedException, BadRequestException, NotAuthorizedException}
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.apache.http.client.RedirectException
import org.scalatra.servlet.SizeConstraintExceededException
import org.scalatra.{RailsPathPatternParser, RequestEntityTooLarge, RouteMatcher, ScalatraServlet}

/**
  * Created by mminin on 30.05.16.
  */
abstract class ServletBase
  extends ScalatraServlet
    with HTTPMethodsSupport
    with LiferayAuthSupport
    with CSRFTokenSupport
    with LogSupport
    with InjectableSupport {

  implicit override def string2RouteMatcher(path: String): RouteMatcher = RailsPathPatternParser(path)

  error { //handle 'before' errors
    case e: NotAuthorizedException          => halt(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage)
    case e: AccessDeniedException           => halt(HttpServletResponse.SC_FORBIDDEN, e.getMessage)
    case e: BadRequestException             => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
    case e: EntityNotFoundException         => halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
    case e: EntityDuplicateException        => halt(HttpServletResponse.SC_CONFLICT, e.getMessage)
    case e: LNoSuchUserException            => halt(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage, reason = "No user exists")
    case e: PassingLimitExceededException   => halt(HttpServletResponse.SC_FORBIDDEN)
    case e: ParameterNamesNotFoundException => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
    case e: SizeConstraintExceededException => RequestEntityTooLarge("too much!")
    case e: Exception =>
      log.error(e)
      halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
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
      case e: LNoSuchUserException            => halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage, reason = "No user exists")
      case e: PassingLimitExceededException   => halt(HttpServletResponse.SC_FORBIDDEN)
      case e: ParameterNamesNotFoundException => halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
      case e: RedirectException               => response.sendRedirect(e.getMessage)
      case e: Exception =>
        log.error(e)
        halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
    }
  }

  def getCompanyId: Long = PermissionUtil.getCompanyId
  def getUserId: Long = PermissionUtil.getUserId
  def getUser: LUser = PermissionUtil.getLiferayUser
}
