package com.arcusys.valamis.updaters.version330.schema3301

case class OldSetting(
                       key: String,
                       value: String
                     )

case class Setting(
                    id: Long,
                    key: String,
                    value: String,
                    companyId: Option[Long]
                  )

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