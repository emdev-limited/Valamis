package com.arcusys.valamis.web.portlet.base

import java.io.{FileNotFoundException, PrintWriter}
import javax.portlet.{GenericPortlet, PortletRequest, RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.configuration.InjectableSupport

import scala.util.{Failure, Success, Try}

trait PortletBase
  extends MustacheSupport
    with i18nSupport
    with TemplateCoupler
    with LogSupport
    with InjectableSupport {
  self: GenericPortlet =>

  lazy val resourceReader = inject[ResourceReader]

  override def destroy() {}

  override def doDispatch(request: RenderRequest, response: RenderResponse): Unit = {
     self.doDispatch(request, response)
  }

  protected def getTranslation(view: String, language: String): Map[String, String] = {
    try {
      getTranslation("/i18n/" + view + "_" + language)
    } catch {
      case e: FileNotFoundException => getTranslation("/i18n/" + view + "_en")
      case _: Throwable => Map[String, String]()
    }
  }

  protected def getContextPath(request: PortletRequest): String = {
    PortalUtilHelper.getPathContext(request)
  }

  protected def sendMustacheFile(data: Any, path: String)(implicit out: PrintWriter) = {
    out.println(mustache(data, path))
  }

  protected def sendTextFile(path: String)(implicit out: PrintWriter) = {
    val resourceStream = resourceReader.getResourceAsStream(this, path)

    val content = try {
      scala.io.Source.fromInputStream(resourceStream).mkString
    }
    finally {
      resourceStream.close()
    }
    out.println(content)
  }
}
