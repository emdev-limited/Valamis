package com.arcusys.valamis.web.portlet.base

import java.io.InputStreamReader
import java.util.Properties
import javax.portlet.{GenericPortlet, PortletContext}

import com.arcusys.valamis.utils.ResourceReader

import scala.collection.JavaConversions._

trait i18nSupport {
  self: GenericPortlet =>

  def resourceReader: ResourceReader

  def getTranslation(path: String): Map[String, String] = {
    val properties = propertiesForPortlet(path, self.getPortletContext).toMap

    mapAsScalaMap(properties).toMap.map { case (k, v) => (k.toString, v.toString) }
  }

  private def propertiesForPortlet(templatePath: String, context: PortletContext) = {
    val properties = new Properties
    val resourceStream = resourceReader.getResourceAsStream(this, templatePath + ".properties")
    val reader = new InputStreamReader(resourceStream, "UTF-8")

    properties.load(reader)
    resourceStream.close()
    properties
  }
}