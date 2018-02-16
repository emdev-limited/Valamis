package com.arcusys.learn.liferay.update.version260.scheme2506

case class LrsEndpoint(endpoint: String,
                       auth: AuthType.AuthType,
                       key: String,
                       secret: String,
                       customHost: Option[String] = None,
                       id: Option[Long] = None)

object AuthType extends Enumeration {
  type AuthType = Value
  val Internal = Value("Internal")
  val Basic = Value("Basic")
  val OAuth = Value("OAuth")
}