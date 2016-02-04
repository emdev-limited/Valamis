package com.arcusys.learn.policies.api

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.controllers.auth.LiferayAuthSupport
import com.arcusys.learn.exceptions.{AccessDeniedException, BadRequestException, NotAuthorizedException}
import com.arcusys.learn.utils.HTTPMethodsSupport
import com.arcusys.valamis.exception.{EntityDuplicateException, EntityNotFoundException}
import com.arcusys.valamis.lesson.exception.PassingLimitExceededException
import com.liferay.portal.NoSuchUserException
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.scalatra.ScalatraServlet

trait BasePolicy extends ScalatraServlet with HTTPMethodsSupport with LiferayAuthSupport
{
  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
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
    case e: Exception =>
      log(e.getMessage, e)
      halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
  }
}
