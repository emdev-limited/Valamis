package com.arcusys.learn.controllers.api

import java.io.InputStream
import javax.servlet.http._

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.facades._
import com.arcusys.learn.liferay.permission.PermissionUtil
import com.arcusys.learn.liferay.services.FileEntryServiceHelper
import com.arcusys.learn.models.request._
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.learn.models.FileResponse
import com.arcusys.learn.policies.api.FilePolicy
import com.arcusys.learn.service.util.ImageProcessor
import com.arcusys.learn.web.FileUploading
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.slide.service.{SlideElementServiceContract, SlideServiceContract, SlideSetServiceContract}
import com.liferay.portal.util.PortalUtil
import org.joda.time.DateTime
import org.scalatra.servlet.MultipartConfig

class FileApiController
  extends BaseApiController
  with FileUploading
  with FilePolicy {

  configureMultipartHandling(MultipartConfig(maxFileSize = Some(30 * 1024 * 1024)))

  private lazy val packageFacade = inject[PackageFacadeContract]
  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val questionFacade = inject[QuestionFacadeContract]
  private lazy val fileFacade = inject[FileFacadeContract]

  private lazy val imageProcessor = inject[ImageProcessor]

  private lazy val certificateService = inject[CertificateService]
  private lazy val packageService = inject[ValamisPackageService]
  private lazy val slideSetService = inject[SlideSetServiceContract]
  private lazy val slideService = inject[SlideServiceContract]
  private lazy val slideElementService = inject[SlideElementServiceContract]

  private val LogoWidth  = 360
  private val LogoHeight = 240

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

    val url = PortalUtil.getPortalURL(request)
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
          fileRequest.isSortDirectionAsc)

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
            fileRequest.isSortDirectionAsc)

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
            fileRequest.isSortDirectionAsc)

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
            getZipStream(packageFacade.exportAllPackages(data.courseId), "exportAllPackages")

          case FileExportRequest.Export =>
            getZipStream(packageFacade.exportPackages(data.ids), "exportPackages")
        }
      case FileExportRequest.Certificate =>
        data.action match {
          case FileExportRequest.ExportAll =>
            getZipStream(certificateFacade.exportCertificates(data.companyID), "exportAllCertificates")

          case FileExportRequest.Export =>
            getZipStream(certificateFacade.exportCertificate(data.companyID, data.id), "exportCertificates")
        }
      case FileExportRequest.Question =>
        data.action match {
          case FileExportRequest.ExportAll =>
            getZipStream(questionFacade.exportAllQuestionsBase(data.courseId), "exportAllQuestionBase")

          case FileExportRequest.Export =>
            getZipStream(questionFacade.exportQuestions(data.categoryIds, data.ids, data.plainTextIds,Option(data.courseId)), "exportQuestions")
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
      case FileActionType.Delete => fileFacade.remove(fileRequest.id.get)
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
    packageService.setLogo(id, name, imageProcessor.resizeImage(data, LogoWidth, LogoHeight))
  })

  post("/files/slideset/:id/logo")(jsonAction {
    val id = params("id").toLong
    val (name, data) = readSendImage
    slideSetService.setLogo(id, name, imageProcessor.resizeImage(data, LogoWidth, LogoHeight))
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
          fileRequest.fileContent)

      case UploadContentType.Pdf =>
        fileFacade.saveFile(
          s"slideData${fileRequest.entityId}",
          fileRequest.fileName,
          fileRequest.fileContent)

      case UploadContentType.ImportFromPdf =>
        slideService.parsePDF(
          fileRequest.fileContent,
          fileRequest.slideId,
          fileRequest.slideSetId
        )

      case UploadContentType.ImportFromPptx =>
        slideService.parsePPTX(
          fileRequest.fileContent,
          fileRequest.slideId,
          fileRequest.slideSetId,
          fileRequest.fileName
        )

      case UploadContentType.Package =>
        val packageRequest = PackageFileRequest(this)
        if (fileRequest.fileName.endsWith(".zip")) {
          fileFacade.uploadPackage(
            PackageFileRequest.DefaultPackageTitle,
            PackageFileRequest.DefaultPackageDescription,
            packageRequest.courseId,
            PermissionUtil.getUserId,
            packageRequest.stream)
        } else {
          if (fileRequest.fileName.endsWith(".pptx")) {
            fileFacade.uploadPresentation(
              fileRequest.fileName,
              fileRequest.stream,
              PackageFileRequest.DefaultPackageTitle,
              PackageFileRequest.DefaultPackageDescription,
              packageRequest.courseId,
              PermissionUtil.getUserId
            )
          }
        }

      case UploadContentType.DocLibrary =>
        val content = FileEntryServiceHelper.getFile(fileRequest.fileEntryId, fileRequest.fileVersion)
        fileFacade.saveFile(
          fileRequest.folder,
          fileRequest.file,
          content)

      case UploadContentType.ImportQuestion =>
        fileFacade.importQuestions(
          fileRequest.courseId,
          fileRequest.stream)

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