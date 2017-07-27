package com.arcusys.valamis.web.servlet.lrsProxy

import java.io.ByteArrayInputStream
import java.net.{URLDecoder, URLEncoder}
import java.util
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper, HttpServletResponse}

import org.apache.commons.codec.CharEncoding
import org.apache.http.HttpHeaders._

import scala.collection.JavaConverters._
import ProxyConstants._


trait MethodOverrideFilter {


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

    private val contentParameters = bodyContent
      .parseParameters(enc, decode = false)
      .filterKeys(_ != Method)

    private val newHeaders = contentParameters.filterKeys(HttpHeaders.contains)
    private val newParams = (contentParameters -- newHeaders.keys).filterKeys(_ != Content)

    private def getContentParameter(name: String): Option[String] = {
      contentParameters.find(_._1.equalsIgnoreCase(name)).map(_._2)
    }

    override def getMethod = method.toUpperCase

    override def getHeader(name: String): String = {
      name.toLowerCase match {
        case "content-length" => getContentLength.toString
        case _ => getContentParameter(name).getOrElse(super.getHeader(name))
      }
    }

    override def getHeaderNames: util.Enumeration[Any] = {
      (super.getHeaderNames.asScala ++ newHeaders.keys).toSeq.distinct.iterator.asJavaEnumeration
    }

    override def getParameterMap: util.Map[String, Array[String]] = {
      newParams.map(p => (p._1, Array(p._2))).asJava
    }

    override def getParameter(name: String): String =
      getContentParameter(name).orNull

    override def getContentType: String = {
      getHeader(CONTENT_TYPE)
    }

    override def getContentLength: Int = {
      getContentParameter(Content).map(_.length).getOrElse(0)
    }

    override def getInputStream = {
      val content = getContentParameter(Content).getOrElse("")

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
      val newParametersString = newParams
        .filterNot(_._1 == "registration") // fix for articulate packages
        .toQueryString(enc)

      val sourceParams = super.getQueryString.parseParameters(enc).filterKeys(!_.equalsIgnoreCase(Method))

      if (sourceParams.isEmpty)
        newParametersString
      else {
        sourceParams.toQueryString(enc) + "&" + newParametersString
      }
    }
  }

  implicit class ParameterStringExtension(str: String){
    def parseParameters(enc: String, decode: Boolean = true): Map[String, String] = str.split("&")
      .map(_.split("=", 2))
      .map(p => (p(0), if(decode) URLDecoder.decode(p(1), enc) else p(1)))
      .toMap
  }

  implicit class ParameterMapExtension(params: Map[String, String]){
    def toQueryString(enc: String): String = params
      .map(p => p._1 + "=" + URLEncoder.encode(p._2, enc))
      .mkString("&")
  }
}
