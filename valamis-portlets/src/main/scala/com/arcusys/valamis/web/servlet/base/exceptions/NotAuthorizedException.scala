package com.arcusys.valamis.web.servlet.base.exceptions

case class NotAuthorizedException(message: String = null) extends Exception(message)
