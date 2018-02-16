package com.arcusys.valamis.web.servlet.file

import javax.servlet.http.HttpServletResponse

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PartialContentSupport}
import org.scalatra.SinatraRouteMatcher
import org.scalatra.servlet.FileUploadSupport

class FileStorageServlet extends BaseApiController with FileUploadSupport with PartialContentSupport {

  //next line fixes 404
  implicit override def string2RouteMatcher(path: String) = new SinatraRouteMatcher(path)

  private val fileService = inject[FileService]

  get("/*.*") {
    val filename = multiParams("splat").mkString(".")
    val extension = multiParams("splat").last.split('.').last

    if (extension == "htm" || extension == "html") {
      sendHtmlFile(filename)
    }
    else {
      sendFile(filename, extension)
    }
  }

  private def sendHtmlFile(filename: String) = {
    val content = getContent(filename)

    contentType = "text/html"
    response.setCharacterEncoding("")

    response.setHeader("Content-Disposition", s"""filename="$filename"""")

    response.getOutputStream.write(content)
  }

  private def sendFile(filename: String, extension: String) = {
    contentType = extension.toLowerCase match {
      case "css" => "text/css"
      case "js" => "application/javascript"
      case "png" => "image/png"
      case "jpg" | "jpeg" => "image/jpeg"
      case "gif" => "image/gif"
      case "swf" => "application/x-shockwave-flash"
      case "mp3" => "audio/mpeg"
      case "ogg" => "audio/ogg"
      case "wma" => "audio/x-ms-wma"
      case "wav" => "audio/wav"
      case _ => FileSystemUtil.getMimeType(filename)
    }
    response.setHeader("Content-Disposition", s"""filename="$filename"""")

    writePartialContent(getContent(filename))
  }

  private def getContent(filename: String) = {
    fileService.getFileContentOption(filename) getOrElse {
      halt(HttpServletResponse.SC_NOT_FOUND)
    }
  }
}
