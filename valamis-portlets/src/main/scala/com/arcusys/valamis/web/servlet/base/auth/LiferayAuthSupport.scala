package com.arcusys.valamis.web.servlet.base.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.{ScentryConfig, ScentrySupport}

trait LiferayAuthSupport extends ScentrySupport[AuthUser] {
  self: ScalatraBase =>

  protected final val StrategyName = "LiferayAuth"

  before() {
    scentry.authenticate(StrategyName)
  }

  override protected val scentryConfig = new ScentryConfig {
    override val login = "/c/portal/login"
    override val returnTo = "/"
    override val returnToKey = "redirect"
    override val failureUrl = "/c/portal/logout"
  }.asInstanceOf[ScentryConfiguration]

  protected def fromSession = {
    case id: String => AuthUser(id.toLong)
  }

  protected def toSession = {
    case usr: AuthUser => usr.id.toString
  }

  override protected def registerAuthStrategies = {
    scentry.register(
      StrategyName,
      app => new LiferayCheckerAuthStrategy(app))
  }


}