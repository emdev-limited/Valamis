package com.arcusys.learn.utils

import java.io.{IOException, UnsupportedEncodingException}
import java.net.URLDecoder
import java.util
import javax.servlet.ServletInputStream

object HttpUtilsHelper {
  def parsePostData(len: Int, in: ServletInputStream, enc: String): util.Hashtable[String, Array[String]] = {
    if (len <= 0) return new util.Hashtable()
    if (in == null) {
      throw new IllegalArgumentException
    }
    val postedBytes: Array[Byte] = new Array[Byte](len)
    try {
      var offset: Int = 0
      do {
        val inputLen: Int = in.read(postedBytes, offset, len - offset)
        if (inputLen <= 0) {
          throw new IllegalArgumentException("Body is Empty")
        }
        offset += inputLen
      } while ((len - offset) > 0)
    }
    catch {
      case e: IOException =>
        throw new IllegalArgumentException(e.getMessage)
    }
    try {
      val postedBody: String = new String(postedBytes, 0, len, enc)
      parseQueryString(postedBody, enc)
    }
    catch {
      case e: UnsupportedEncodingException =>
        throw new IllegalArgumentException(e.getMessage)
    }
  }

  def parseQueryString (s: String, enc: String): util.Hashtable[String, Array[String]] = {
    if (s == null) {
      throw new IllegalArgumentException
    }

    val ht = new util.Hashtable[String, Array[String]]
    s.split("&")
      .filter(_.nonEmpty)
      .foreach{st =>
        val pair = st.split("=")
        val key = URLDecoder.decode(pair(0), enc)
        val v = if(pair.length == 2) URLDecoder.decode(pair(1), enc) else ""
        val items = if (ht.containsKey(key)) ht.get(key) :+ v else Array(v)
        ht.put(key, items)
    }
    ht
  }
}
