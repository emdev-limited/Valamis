package com.arcusys.valamis.utils

import java.io.InputStream
import javax.portlet.GenericPortlet
import javax.servlet.ServletContext

/**
  * Created by ematyuhin on 08.06.16.
  */
trait ResourceReader {
  def getResourceAsStream(portlet: GenericPortlet, path: String): InputStream
  def getResourceAsStream(servletContext: ServletContext, path: String): InputStream
}
