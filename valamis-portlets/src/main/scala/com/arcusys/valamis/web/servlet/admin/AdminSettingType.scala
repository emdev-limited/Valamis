package com.arcusys.valamis.web.servlet.admin

object AdminSettingType extends Enumeration {
  type AdminSettingType = Value

  val Lrs = Value("lrs")
  val Issuer = Value("issuer")
  val GoogleAPI = Value("google-api")
  val Lti = Value("lti")
  val BetaStudio = Value("beta-studio")
}