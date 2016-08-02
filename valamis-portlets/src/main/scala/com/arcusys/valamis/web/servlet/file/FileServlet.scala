package com.arcusys.valamis.web.servlet.file

import java.io.InputStream
import javax.servlet.http._

import com.arcusys.learn.liferay.services.FileEntryServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.certificate.service.export.CertificateExportProcessor
import com.arcusys.valamis.content.export.QuestionExportProcessor
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.lesson.service.export.PackageExportProcessor
import com.arcusys.valamis.lesson.service.{LessonService, PackageUploadManager}
import com.arcusys.valamis.slide.service.{SlideElementServiceContract, SlideServiceContract, SlideSetServiceContract}
import com.arcusys.valamis.web.service.{ImageProcessor, Sanitizer}
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.file.request._
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import org.joda.time.DateTime
import org.scalatra.servlet.MultipartConfig

class FileServlet
  extends BaseApiController
  with FileUploading
  with FilePolicy {

  configureMultipartHandling(MultipartConfig(maxFileSize = Some(30 * 1024 * 1024)))

  private lazy val fileFacade = inject[FileFacadeContract]

  private lazy val imageProcessor = inject[ImageProcessor]

  private lazy val certificateService = inject[CertificateService]
  private lazy val slideSetService = inject[SlideSetServiceContract]
  private lazy val courseService = inject[CourseService]
  private lazy val slideService = inject[SlideServiceContract]
  private lazy val slideElementService = inject[SlideElementServiceContract]

  private lazy val lessonService = inject[LessonService]

  private lazy val uploadManager = inject[PackageUploadManager]

  implicit lazy val sanitizer = inject[Sanitizer]

  private lazy val LogoWidth  = 360
  private lazy val LogoHeight = 240
  private lazy val ImageWidth = 1024
  private lazy val ImageHeight = 768

  get("/files/images")(action {
    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")

    val fileRequest = FileRequest(this)
    val content = fileFacade.getFileContent(fileRequest.folder, fileRequest.file)
    response.getOutputStream.write(content)
  })

  get("/files/video")(action {
    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("text/html")

    val fileRequest = FileRequest(this)
    val courseId = fileRequest.videoCourseId
    val uuid = fileRequest.videoUUId

    val url = PortalUtilHelper.getPortalURL(request)
    val videosrcDL = url + "/c/document_library/get_file?uuid=" + uuid + "&groupId=" + courseId

    val styles = "video::-webkit-media-controls-fullscreen-button { display: none!important;}"

    <html>
      <head>
        <style>
          {styles}
        </style>
      </head>
      <body style="margin:0; background: black;">
        <video width="100%" height="100%" controls="true">
          <source src={videosrcDL}/>
        </video>
      </body>
    </html>

  })

  get("/files/packages/(:" + FileRequest.FileId + ")")(action {
    val fileRequest = FileRequest(this)
    fileRequest.action match {
      case FileActionType.All =>
        val packages = fileFacade.getPackages(
          fileRequest.skip,
          fileRequest.count,
          fileRequest.filter,
          fileRequest.ascending)

        val total = fileFacade.packageCount(
          fileRequest.skip,
          fileRequest.count,
          fileRequest.filter)

        CollectionResponse(
          fileRequest.page,
          packages,
          total)
      case FileActionType.Scorm =>
        if (fileRequest.id.isDefined)
          fileFacade.getScormPackage(fileRequest.id.get)
        else {
          val packages = fileFacade.getScormPackages(
            fileRequest.skip,
            fileRequest.count,
            fileRequest.filter,
            fileRequest.ascending)

          val total = fileFacade.scormPackageCount(
            fileRequest.skip,
            fileRequest.count,
            fileRequest.filter)

          CollectionResponse(
            fileRequest.page,
            packages,
            total)
        }
      case FileActionType.Tincan =>
        if (fileRequest.id.isDefined)
          fileFacade.getTincanPackage(fileRequest.id.get)
        else {
          val packages = fileFacade.getTincanPackages(
            fileRequest.skip,
            fileRequest.count,
            fileRequest.filter,
            fileRequest.ascending)

          val total = fileFacade.tincanPackageCount(
            fileRequest.skip,
            fileRequest.count,
            fileRequest.filter)

          CollectionResponse(
            fileRequest.page,
            packages,
            total)
        }
    }
  })

  get("/files/export(/)")(action {

    def getZipStream(fileStream: InputStream, nameOfFile: String) = {
      response.setHeader("Content-Type", "application/zip")
      response.setHeader("Content-Disposition", s"attachment; filename=${nameOfFile}_${DateTime.now.toString("YYYY-MM-dd_HH-mm-ss")}${FileExportRequest.ExportExtension}")
      fileStream
    }

    val data = FileExportRequest(this)
    data.contentType match {
      case FileExportRequest.Package =>
        data.action match {
          case FileExportRequest.ExportAll =>
            val stream = new PackageExportProcessor().exportItems(lessonService.getAllWithLimits(data.courseId))
            getZipStream(stream, "exportAllPackages")

          case FileExportRequest.Export =>
            val ids = data.ids
            val stream = new PackageExportProcessor().exportItems(ids.map(lessonService.getWithLimit))
            getZipStream(stream, "exportPackages")
        }
      case FileExportRequest.Certificate =>
        data.action match {
          case FileExportRequest.ExportAll =>
            getZipStream(
              new CertificateExportProcessor().export(data.companyId),
              "exportAllCertificates"
            )

          case FileExportRequest.Export =>
            getZipStream(
              new CertificateExportProcessor().export(data.companyId, data.id),
              "exportCertificates"
            )
        }
      case FileExportRequest.Question =>
        data.action match {
          case FileExportRequest.ExportAll =>
            getZipStream(new QuestionExportProcessor().exportAll(data.courseId), "exportAllQuestionBase")

          case FileExportRequest.Export =>
            getZipStream(
              new QuestionExportProcessor().exportIds(data.categoryIds, data.ids, data.plainTextIds, Some(data.courseId)),
              "exportQuestions"
            )
        }
      case FileExportRequest.SlideSet =>
        data.action match {
          case FileExportRequest.Export => getZipStream(slideSetService.exportSlideSet(data.id), "ExportedSlideSet")
        }
    }
  })

  post("/files(/)")(jsonAction {
    val fileRequest = FileRequest(this)
    fileRequest.action match {
      case FileActionType.Add => addFile(fileRequest)
      case FileActionType.Update => updateFile(PackageFileRequest(this))
    }
  })

  post("/files/certificate/:id/logo")(jsonAction {
    val id = params("id").toLong
    val (name, data) = readSendImage
    certificateService.setLogo(id, name, imageProcessor.resizeImage(data, LogoWidth, LogoHeight))
  })

  post("/files/package/:id/logo")(jsonAction {
    val id = params("id").toLong
    val (name, data) = readSendImage
    lessonService.setLogo(id, name, imageProcessor.resizeImage(data, LogoWidth, LogoHeight))
  })

  post("/files/slideset/:id/logo")(jsonAction {
    val id = params("id").toLong
    val (name, data) = readSendImage
    slideSetService.setLogo(id, name, imageProcessor.resizeImage(data, LogoWidth, LogoHeight))
  })

  post("/files/course/:id/logo")(jsonAction {
    val id = params("id").toLong
    val (name, data) = readSendImage
    courseService.setLogo(id, imageProcessor.resizeImage(data, LogoWidth, LogoHeight))
  })

  post("/files/slide/:id/logo")(jsonAction {
    val id = params("id").toLong
    val (name, data) = readSendImage
    slideService.setLogo(id, name, data)
  })

  post("/files/slideentity/:id/logo")(jsonAction {
    val id = params("id").toLong
    val (name, data) = readSendImage
    slideElementService.setLogo(id, name, data)
  })

  private def readSendImage: (String, Array[Byte]) = {
    val fileRequest = FileRequest(this)
    fileRequest.contentType match {
      case UploadContentType.Icon => (
        fileRequest.fileName,
        fileRequest.fileContent
      )
      case UploadContentType.DocLibrary => (
        fileRequest.file,
        FileEntryServiceHelper.getFile(fileRequest.fileEntryId, fileRequest.fileVersion)
      )
      case UploadContentType.Base64Icon => (
        FileRequest.DefaultIconName,
        fileRequest.base64Content
      )
    }
  }

  private def addFile(fileRequest: FileRequest.Model) = {
    fileRequest.contentType match {
      case UploadContentType.Base64Icon =>
        fileFacade.saveFile(
          fileRequest.folder,
          FileRequest.DefaultIconName,
          fileRequest.base64Content)

      case UploadContentType.Icon =>
        fileFacade.saveFile(
          fileRequest.folder,
          fileRequest.fileName,
          imageProcessor.resizeImage(fileRequest.fileContent, ImageWidth, ImageHeight))

      case UploadContentType.WebGLModel =>
        fileFacade.saveFile(
          fileRequest.folder,
          fileRequest.fileName,
          fileRequest.fileContent)

      case UploadContentType.Pdf =>
        fileFacade.saveFile(
          s"slideData${fileRequest.entityId}",
          fileRequest.fileName,
          fileRequest.fileContent)

      case UploadContentType.ImportFromPdf =>
        slideService.parsePDF(
          fileRequest.fileContent
        )

      case UploadContentType.ImportFromPptx =>
        slideService.parsePPTX(
          fileRequest.fileContent,
          fileRequest.fileName
        )

      case UploadContentType.Package =>
        uploadPackage(fileRequest)

      case UploadContentType.DocLibrary =>
        val content = FileEntryServiceHelper.getFile(fileRequest.fileEntryId, fileRequest.fileVersion)
        fileFacade.saveFile(
          fileRequest.folder,
          fileRequest.file,
          content)

      case UploadContentType.ImportQuestion =>
        try {
          fileFacade.importQuestions(
            fileRequest.courseId,
            fileRequest.stream)
        } catch {
          case e: UnsupportedOperationException => throw new BadRequestException(e.getMessage)
        }

     case UploadContentType.ImportMoodleQuestion =>
        fileFacade.importMoodleQuestions(
          fileRequest.courseId,
          fileRequest.stream)

      case UploadContentType.ImportCertificate =>
        fileFacade.importCertificates(
          fileRequest.companyIdRequired,
          fileRequest.stream)

      case UploadContentType.ImportPackage =>
        fileFacade.importPackages(
          fileRequest.courseId,
          fileRequest.stream,
          PermissionUtil.getUserId)

      case UploadContentType.ImportSlideSet =>
        slideSetService.importSlideSet(
          fileRequest.stream,
          fileRequest.courseId
        )
        FileResponse(-1,
          "SlideSet",
          "SlideSet",
          "")
    }
  }

  private def uploadPackage(fileRequest: FileRequest.Model) = {
    val packageRequest = PackageFileRequest(this)
    if (fileRequest.fileName.endsWith(".zip") || fileRequest.fileName.endsWith(".pptx")) {
      try {
        val lesson = uploadManager.uploadPackage(
          PackageFileRequest.DefaultPackageTitle,
          PackageFileRequest.DefaultPackageDescription,
          packageRequest.courseId,
          PermissionUtil.getUserId,
          packageRequest.fileName,
          packageRequest.stream)

        FileResponse(
          lesson.id,
          lesson.lessonType match {
            case LessonType.Scorm => "scorm"
            case LessonType.Tincan => "tincan"
          },
          "%s.%s".format(lesson.title, PackageFileRequest.PackageFileExtension),
          "")
      } catch {
        case e: UnsupportedOperationException => throw new BadRequestException(e.getMessage)
      }
    }
  }

  private def updateFile(packageRequest: PackageFileRequest.Model) {
    packageRequest.contentType match {
      case UploadContentType.Icon | UploadContentType.Base64Icon =>
        fileFacade.attachImageToPackage(
          packageRequest.id.get,
          packageRequest.imageID)

      case UploadContentType.Package =>
        fileFacade.updatePackage(
          packageRequest.id.get,
          packageRequest.title,
          packageRequest.summary)
    }
  }
}