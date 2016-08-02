package com.arcusys.valamis.web.servlet.certificate.response

case class AvailableStatementResponse(
  verb: String,
  verbName: Map[String, String],
  obj: String,
  objName: Map[String, String],
  timestamp: String)
