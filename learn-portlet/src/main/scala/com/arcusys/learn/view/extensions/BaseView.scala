package com.arcusys.learn.view.extensions

import java.io.{FileInputStream, PrintWriter, FileNotFoundException}
import javax.portlet.{GenericPortlet, PortletRequest}

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.service.util.MustacheSupport
import com.arcusys.valamis.util.LogSupport
import com.escalatesoft.subcut.inject.Injectable

/**
 * Created by mminin on 27.05.15.
 */
trait BaseView
  extends MustacheSupport
  with i18nSupport
  with Injectable
  with TemplateCoupler
  with LogSupport {
  self: GenericPortlet =>

  override implicit lazy val bindingModule = Configuration

  override def destroy() {}

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

  def getRealPath(path: String): String = {
    getPortletContext.getRealPath(path)
  }

  protected def sendMustacheFile(data:Any, path:String)(implicit out: PrintWriter) = {
    out.println(mustache(data, path))
  }

  protected def sendTextFile(path:String)(implicit out: PrintWriter) = {
    val resourceStream = new FileInputStream(getRealPath(path))
    val content = try {
      scala.io.Source.fromInputStream(resourceStream).mkString
    }
    finally {
      resourceStream.close()
    }
    out.println(content)
  }
}
