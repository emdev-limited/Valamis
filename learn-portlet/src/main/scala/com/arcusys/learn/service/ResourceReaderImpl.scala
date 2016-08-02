package com.arcusys.learn.service

import java.io.{FileInputStream, InputStream}
import javax.portlet.GenericPortlet
import javax.servlet.ServletContext

import com.arcusys.valamis.utils.ResourceReader

class ResourceReaderImpl extends ResourceReader {
  def getResourceAsStream(portlet: GenericPortlet, path: String): InputStream = {
    val realPath = portlet.getPortletContext.getRealPath(path)
    new FileInputStream(realPath)
  }

  def getResourceAsStream(servletContext: ServletContext, path: String): InputStream = {
    val realPath = servletContext.getRealPath(path)
    new FileInputStream(realPath)
  }
}
