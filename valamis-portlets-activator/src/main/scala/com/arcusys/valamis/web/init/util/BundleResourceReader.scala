package com.arcusys.valamis.web.init.util

import java.io.InputStream
import javax.portlet.GenericPortlet
import javax.servlet.ServletContext

import com.arcusys.valamis.utils.ResourceReader

class BundleResourceReader extends ResourceReader {
  val ResourcesPath = "META-INF/resources"

  def getResourceAsStream(portlet: GenericPortlet, path: String): InputStream = {
    val suffix = if(path.startsWith("/")) "" else "/"

    // ugly hack =(
    Option {
      this.getClass.getClassLoader.getResourceAsStream("/" + ResourcesPath + suffix + path)
    } getOrElse {
      portlet.getClass.getClassLoader.getResourceAsStream("/" + ResourcesPath + suffix + path)
    }
  }

  def getResourceAsStream(servletContext: ServletContext, path: String): InputStream = {
    val suffix = if(path.startsWith("/")) "" else "/"
    this.getClass.getClassLoader.getResourceAsStream("/" + ResourcesPath + suffix + path)
  }
}
