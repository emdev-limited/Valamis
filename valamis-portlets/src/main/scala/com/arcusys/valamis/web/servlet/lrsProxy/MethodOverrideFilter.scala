package com.arcusys.valamis.web.servlet.lrsProxy

import java.io.ByteArrayInputStream
import java.net.{URLDecoder, URLEncoder}
import java.util
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper, HttpServletResponse}

import org.apache.commons.codec.CharEncoding
import org.apache.http.HttpHeaders._

import scala.collection.JavaConverters._


trait MethodOverrideFilter {
  private val Method = "method"
  private val Content = "content"
  private val TincanHeaders = Seq("authorization", "content-type", "x-experience-api-version", Content)

  def doFilter(req: HttpServletRequest,
                        res: HttpServletResponse) : HttpServletRequest = {

    req.getMethod match {
      case "POST" =>
        req.getParameter(Method) match {
          case null => req
          case method => getOverridedRequest(req, method)
        }
      case _ =>
        req
    }
  }

  // this request impl should hide http method overriding from client code
  def getOverridedRequest(req: HttpServletRequest, method:String): HttpServletRequestWrapper = new HttpServletRequestWrapper(req) {

    private val encoding = req.getCharacterEncoding
    private val enc = if (encoding == null || encoding.trim.length == 0) "UTF-8" else encoding
    private final val bodyContent = URLDecoder.decode(scala.io.Source.fromInputStream(req.getInputStream).mkString, enc)

    private val newParameters = bodyContent.split("&")
      .map(_.split("=", 2))
      .map(p => (p(0), p(1))).toMap

    private def getNewParameter(name: String): Option[String] = {
      newParameters.find(_._1.equalsIgnoreCase(name)).map(_._2)
    }


    override def getMethod = method.toUpperCase

    override def getHeader(name: String): String = {
      name.toLowerCase match {
        case "content-length" => getContentLength.toString
        case _ => getNewParameter(name).getOrElse(super.getHeader(name))
      }
    }

    override def getHeaderNames: util.Enumeration[Any] = {
      (super.getHeaderNames.asScala ++ newParameters.keys).toSeq.distinct.iterator.asJavaEnumeration
    }

    override def getParameterMap: util.Map[String, Array[String]] = {
      newParameters.map(p => (p._1, Array(p._2))).asJava
    }

    override def getParameter(name: String): String =
      newParameters.find(_._1.equalsIgnoreCase(name)).map(_._2).orNull

    override def getContentType: String = {
      getHeader(CONTENT_TYPE)
    }

    override def getContentLength: Int = {
      getNewParameter(Content).map(_.length).getOrElse(0)
    }

    override def getInputStream = {
      val content = getNewParameter(Content).getOrElse("")

      val byteArrayInputStream = new ByteArrayInputStream(content.getBytes(CharEncoding.UTF_8))
      new ServletInputStream {

        def read() = byteArrayInputStream.read()

        override def close() = {
          byteArrayInputStream.close()
          super.close()
        }
      }
    }

    override def getQueryString: String = {
      val originalParametersPairs = super.getQueryString.split("&")

      val newParametersPairs = newParameters
        .filterNot(p => TincanHeaders.contains(p._1.toLowerCase))
        .filterNot(_._1 == "registration") // fix for articulate packages
        .map(p => p._1 + "=" + URLEncoder.encode(p._2, enc))

      (originalParametersPairs ++ newParametersPairs).mkString("&")
    }
  }
}
