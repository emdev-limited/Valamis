package com.arcusys.valamis.web.servlet.base

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.LiferayClasses.{LMustBeAuthenticatedException, LNoSuchUserException, LUser}
import com.arcusys.valamis.exception.{EntityDuplicateException, EntityNotFoundException}
import com.arcusys.valamis.lesson.exception.PassingLimitExceededException
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.web.configuration.InjectableSupport
import com.arcusys.valamis.web.servlet.base.auth.LiferayAuthSupport
import com.arcusys.valamis.web.servlet.base.exceptions.{AccessDeniedException, BadRequestException, NotAuthorizedException}
import com.arcusys.valamis.web.servlet.base.model.ErrorResponse
import com.arcusys.valamis.web.servlet.base.model.ErrorCodes._
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.apache.http.ParseException
import org.apache.http.client.RedirectException
import org.json4s.MappingException
import org.scalatra.servlet.SizeConstraintExceededException
import org.scalatra._

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

  error {
    case e: BadRequestException             => haltWithBadRequest(e)
    case e: ParameterNamesNotFoundException => haltWithBadRequest(e)
    case e: ParseException                  => haltWithBadRequest(e)
    case e: ScalatraException               => haltWithBadRequest(e)
    case e: MappingException                => haltWithBadRequest("Bad JSON value")
    case e: EntityNotFoundException         => haltWithNotFound(e)
    case e: NotAuthorizedException          => haltWithErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, e)
    case e: LNoSuchUserException            => haltWithErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, e)
    case e: AccessDeniedException           => haltWithErrorResponse(HttpServletResponse.SC_FORBIDDEN, e)
    case e: LMustBeAuthenticatedException   => haltWithErrorResponse(HttpServletResponse.SC_FORBIDDEN, e)
    case e: PassingLimitExceededException   =>
      haltWithErrorResponse(HttpServletResponse.SC_FORBIDDEN, "Passing limit exceeded")
    case e: EntityDuplicateException        => haltWithErrorResponse(HttpServletResponse.SC_CONFLICT, e)
    case e: SizeConstraintExceededException =>
      haltWithErrorResponse(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Payload too large")
    case e: RedirectException               => response.sendRedirect(e.getMessage)
    case e: Throwable =>
      log.error(e)
      haltWithErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "See details in server logs")
  }

  protected def haltWithErrorResponse(status: Int, ex: Throwable): Nothing = {
    haltWithErrorResponse(status, ex.getMessage)
  }

  protected def haltWithErrorResponse(status: Int, message: String, details: Option[String] = None): Nothing = {
    halt(status, ErrorResponse(NoCode, message, details))
  }

  //BadRequest and NotFound is used really often, so there are special methods for it
  protected def haltWithBadRequest(message: String): Nothing =
    haltWithErrorResponse(HttpServletResponse.SC_BAD_REQUEST, message)

  protected def haltWithBadRequest(ex: Throwable): Nothing =
    haltWithErrorResponse(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage)

  protected def haltWithNotFound(message: String): Nothing =
    haltWithErrorResponse(HttpServletResponse.SC_NOT_FOUND, message)

  protected def haltWithNotFound(ex: Throwable): Nothing =
    haltWithErrorResponse(HttpServletResponse.SC_NOT_FOUND, ex.getMessage)

  def getCompanyId: Long = PermissionUtil.getCompanyId
  def getUserId: Long = PermissionUtil.getUserId
  def getUser: LUser = PermissionUtil.getLiferayUser
}
