package com.arcusys.learn.controllers.auth

import org.scalatra.auth.ScentrySupport
import org.scalatra.{RequestResponseScope, ScalatraBase}

/**
 * Created by Iliya Tryapitsin on 04.04.2014.
 */

trait LiferayAuthSupport extends ScentrySupport[AuthUser]
    with RequestResponseScope
    with AuthSupport[AuthUser] { self: ScalatraBase =>

  protected val LIFERAY_STRATEGY_NAME = "LiferayAuth"

  protected def fromSession = { case id: String => AuthUser(id.toLong) }
  protected def toSession = { case usr: AuthUser => usr.id.toString }

  override protected def registerAuthStrategies = {
    scentry.register(
      LIFERAY_STRATEGY_NAME,
      app => new LiferayCheckerAuthStrategy(app))
  }
}