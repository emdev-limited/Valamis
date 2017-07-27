package com.arcusys.valamis.web.servlet.base

import java.net.URI

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase

/**
  * Created by pkornilov on 2/20/17.
  */
class HttpDeleteWithBody(uri: String) extends HttpEntityEnclosingRequestBase {

  setURI(URI.create(uri))

  val MethodName = "DELETE"

  override def getMethod: String = MethodName

}
