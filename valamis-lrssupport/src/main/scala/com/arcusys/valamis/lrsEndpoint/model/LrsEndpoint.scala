package com.arcusys.valamis.lrsEndpoint.model

case class LrsEndpoint(endpoint: String,
                       auth: AuthType.AuthType,
                       key: String,
                       secret: String,
                       customHost: Option[String] = None,
                       id: Option[Long] = None)

object AuthType extends Enumeration {
  type AuthType = Value
  val INTERNAL = Value("Internal")
  val BASIC = Value("Basic")
  val OAUTH = Value("OAuth")

  def isValid(s: String) = values.exists(_.toString == s)
}