package com.arcusys.valamis.web.servlet.file

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.web.servlet.base.BaseApiController
import org.scalatra.SinatraRouteMatcher
import org.scalatra.servlet.FileUploadSupport

class FileStorageServlet extends BaseApiController with FileUploadSupport {

  //next line fixes 404
  implicit override def string2RouteMatcher(path: String) = new SinatraRouteMatcher(path)

  private val fileService = inject[FileService]

  get("/*.*") {
    val filename = multiParams("splat").mkString(".")
    val extension = multiParams("splat").last.split('.').last
    contentType = extension match {
      case "css"  => "text/css"
      case "htm"  => "text/html"
      case "html" => "text/html"
      case "js"   => "application/javascript"
      case "png"  => "image/png"
      case "jpg"  => "image/jpeg"
      case "jpeg" => "image/jpeg"
      case "gif"  => "image/gif"
      case "swf"  => "application/x-shockwave-flash"
      case _      => FileSystemUtil.getMimeType(filename)
    }
    val fileContentOption = fileService.getFileContentOption(filename)
    if (fileContentOption.isDefined) {
      response.getOutputStream.write(fileContentOption.getOrElse(halt(405)))
    } else halt(404)
  }
}
