package com.arcusys.valamis.web.servlet.base

import java.io.IOException
import javax.servlet.http.HttpServletResponse
import org.scalatra.ScalatraServlet

trait PartialContentSupport {
  self: ScalatraServlet =>

  def writePartialContent(file: Array[Byte]): Unit = {
    val rangeHeader = request.headers.get("range")
    val content = rangeHeader match {
      case Some(value) =>
        val rangeValue = value.trim().substring("bytes=".length())
        val fileLength = file.length

        val (end, start) = if (rangeValue.startsWith("-")) {
          (fileLength - 1, fileLength - 1 - rangeValue.substring("-".length()).toInt)
        } else {
          val ranges = rangeValue.split("-")
          val rangesEnd =
            if (ranges.length > 1 && ranges(1).toInt <= fileLength - 1)
              ranges(1).toInt
            else
              fileLength - 1
          (rangesEnd, ranges(0).toInt)
        }

        if (start <= end) {
          val contentLength = end - start + 1
          response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)
          response.setHeader("Content-Length", contentLength.toString)
          response.setHeader("Content-Range", "bytes " + start + "-"
            + end + "/" + fileLength)
          file.slice(start, end)
        }
        else {
          file
        }

      case None => file
    }

    try {
      response.getOutputStream.write(content)
    } catch {
      case e: IOException => //browser can close connection before the whole file will be read
    }
  }
}
