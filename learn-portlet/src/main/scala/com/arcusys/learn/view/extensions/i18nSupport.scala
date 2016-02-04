package com.arcusys.learn.view.extensions

import java.io.{FileInputStream, InputStreamReader}
import java.util.Properties
import javax.portlet.{GenericPortlet, PortletContext}
import javax.servlet.ServletContext

import org.scalatra.{ScalatraFilter, ScalatraServlet}

import scala.collection.JavaConversions._

trait i18nSupport {
  def getTranslation(path: String): Map[String, String] = {
    val properties = if (isPortletContext)
      propertiesForPortlet(path, this.asInstanceOf[GenericPortlet].getPortletContext)
    else {
      val context = this match {
        case f: ScalatraFilter  => f.servletContext
        case s: ScalatraServlet => s.servletContext
      }
      propertiesForServlet(path, context)
    }
    mapAsScalaMap(properties.asInstanceOf[java.util.Map[String, String]]).toMap
  }

  private def isPortletContext: Boolean = this.isInstanceOf[GenericPortlet] && this.asInstanceOf[GenericPortlet].getPortletConfig != null

  private def propertiesForPortlet(templatePath: String, context: PortletContext) = propertiesFromRealPath(context.getRealPath(templatePath))

  private def propertiesForServlet(templatePath: String, context: ServletContext) = propertiesFromRealPath(context.getRealPath(templatePath))

  private def propertiesFromRealPath(templateRealPath: String): Properties = {
    val properties = new Properties
    val resourceStream = new InputStreamReader(new FileInputStream(templateRealPath + ".properties"), "UTF-8")
    properties.load(resourceStream)
    resourceStream.close()
    properties
  }
}