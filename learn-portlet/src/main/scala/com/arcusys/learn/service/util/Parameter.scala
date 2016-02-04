package com.arcusys.learn.service.util

import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.scalatra.ScalatraBase

/**
 * Created by Iliya Tryapitsin on 24.02.14.
 */
object Parameter {
  //def apply(name: String)(implicit request: HttpServletRequest) = new ParameterBase(name, request)
  def apply(name: String)(implicit scalatra: ScalatraBase) = new ParameterBase(name, scalatra)

  def ParameterNamesNotFoundException(name: String) = throw new ParameterNamesNotFoundException("Required parameter '" + name + "' is not specified")
}
