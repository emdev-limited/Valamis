package com.arcusys.valamis.web.servlet.base.model

/**
  * Created by pkornilov on 1/30/17.
  */
case class ErrorResponse(
  code: Int,
  message: String,
  details: Option[String]
)

object ErrorCodes {
  val NoCode = -1
}
