package com.arcusys.valamis.web.servlet.base

import java.util
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper, HttpServletResponse}

import org.scalatra.servlet.ServletApiImplicits
import org.scalatra.{Delete, Handler, Patch, Put}

import scala.collection.JavaConversions._

// tomcat (with default settings, liferay bundle) do not read parameters from body for Put | Delete | Patch
// here we read parameters
trait HTTPMethodsSupport extends Handler with ServletApiImplicits {

  abstract override def handle(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    val req2 = req.requestMethod match {
      case Put | Delete | Patch =>
        if (req.getContentType.toLowerCase.contains("application/x-www-form-urlencoded")) {
          new HttpServletRequestWrapper(req) {
            val bodyParams = HttpUtilsHelper.parsePostData(req.getContentLength, req.getInputStream, req.getCharacterEncoding)
            override def getParameter(name: String) = {
              val fromRequest = Option(req.getParameter(name))
              lazy val fromBody = Option(bodyParams.get(name)).map(_.head)
              fromRequest orElse fromBody orNull
            }
            override def getParameterNames = super.getParameterNames ++ bodyParams.keys()
            override def getParameterMap = {
              val paramM: util.HashMap[String, Array[String]] = new util.HashMap
              (super.getParameterMap.entrySet() ++ bodyParams.entrySet())
                .foreach(e => paramM.put(e.getKey.toString, e.getValue.asInstanceOf[Array[String]]))
              paramM
            }
          }
        } else req
      case _ => req
    }
    super.handle(req2, res)
  }
}