package com.arcusys.learn.facades

import java.io._

import com.arcusys.json.JsonHelper
import com.arcusys.learn.models._
import com.arcusys.learn.models.request.{FileRequest, PackageFileRequest}
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.lesson.service.PackageUploadManager
import com.arcusys.valamis.lesson.service.export.PackageImportProcessor
import com.arcusys.valamis.slide.convert.PresentationProcessor
import com.arcusys.valamis.util.FileSystemUtil
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import sun.reflect.generics.reflectiveObjects.NotImplementedException

class FileFacade(implicit val bindingModule: BindingModule) extends FileFacadeContract with Injectable {

  private val fileService = inject[FileService]
  private val certificateFacade = inject[CertificateFacadeContract]
  private val questionFacade = inject[QuestionFacadeContract]
  private val packageUploadService = new PackageUploadManager()
  private lazy val presentationProcessor = inject[PresentationProcessor]

  def saveFile(folder: String, name: String, content: Array[Byte]): FileResponse = {
    fileService.setFileContent(folder, name, content)
    new FileResponse(0, "", name, "")
  }

  def copyToFolder(sourceFolder: String, name: String, destFolder: String) {
    fileService.copyFile(sourceFolder, name, destFolder, name)
  }

  def getFileContent(folder: String, name: String): Array[Byte] = {
    fileService.getFileContent(folder, name)
  }

  def updatePackage(id: Int, title: Option[String], summary: Option[String]) = {
    throw new NotImplementedException
  }

  def remove(id: Int) = throw new NotImplementedException

  def attachImageToPackage(packageId: Int, imageId: Int) = throw new NotImplementedException

  def getPackages(skip: Int,
    take: Int,
    filter: String,
    sortAZ: Boolean): Seq[FileResponse] = throw new NotImplementedException

  def getScormPackages(skip: Int,
    take: Int,
    filter: String,
    sortAZ: Boolean): Seq[FileResponse] = throw new NotImplementedException

  def getTincanPackages(skip: Int,
    take: Int,
    filter: String,
    sortAZ: Boolean): Seq[FileResponse] = throw new NotImplementedException

  def packageCount(skip: Int,
    take: Int,
    filter: String): Int = throw new NotImplementedException

  def scormPackageCount(skip: Int,
    take: Int,
    filter: String): Int = throw new NotImplementedException

  def tincanPackageCount(skip: Int,
    take: Int,
    filter: String): Int = throw new NotImplementedException

  def getScormPackage(scormPackageId: Int): FileResponse = throw new NotImplementedException

  def getTincanPackage(tincanPackageId: Int): FileResponse = throw new NotImplementedException

  def uploadPackage(title: String, summary: String, courseId: Long, userId: Long, stream: InputStream): FileResponse = {
    val file = FileSystemUtil.streamToTempFile(stream, "Upload", PackageFileRequest.PackageFileExtension)
    stream.close()
    val (packageId, packageType) = packageUploadService.uploadPackage(title, summary, courseId, userId, file)

    FileResponse(
      packageId.toInt,
      packageType match {
        case LessonType.Scorm  => "scorm"
        case LessonType.Tincan => "tincan"
      },
      "%s.%s".format(title, PackageFileRequest.PackageFileExtension),
      "") // TODO package url?
  }

  override def uploadPresentation(fileName: String, stream: InputStream, title: String, description: String, courseId: Long, userId: Long) = {

    val name = fileName.reverse.dropWhile(_ != '.').drop(1).reverse
    val packageFile = presentationProcessor.processPresentation(name, stream, title, description, fileName)

    val (packageId, packageType) = packageUploadService.uploadPackage(title, description, courseId, userId, packageFile)

    packageFile.delete()

    FileResponse(
      packageId.toInt,
      packageType match {
        case LessonType.Scorm  => "scorm"
        case LessonType.Tincan => "tincan"
      },
      "%s.%s".format(title, PackageFileRequest.PackageFileExtension),
      "") // TODO package url?
  }

  override def importQuestions(courseId: Int, stream: InputStream): FileResponse = {
    val file = FileSystemUtil.streamToTempFile(stream, "Import", FileRequest.ExportExtension)
    stream.close()
    questionFacade.importQuestions(file, courseId)

    FileResponse(-1, "Question", file.getName, "")
  }

  override def importMoodleQuestions(courseId: Int, stream: InputStream): FileResponse = {
    var file:File = null
    try {
      file = FileSystemUtil.streamToTempFile(stream, "Import", "xml")
      stream.close()
      val res = questionFacade.importMoodleQuestions(file, courseId)
      FileResponse(-1, "Moodle question", file.getName, "",JsonHelper.toJson(res))
    } finally {
      if (file!=null)
        FileSystemUtil.deleteFile(file)
    }
  }

  override def importPackages(courseId: Int, stream: InputStream, userId: Long): FileResponse = {
    val file = FileSystemUtil.streamToTempFile(stream, "Import", FileRequest.ExportExtension)
    stream.close()
    new PackageImportProcessor().importItems(file, courseId, userId)

    FileResponse(-1, "Package", file.getName, "")
  }

  override def importCertificates(companyId: Int, stream: InputStream): FileResponse = {
    val file = FileSystemUtil.streamToTempFile(stream, "Import", FileRequest.ExportExtension)
    stream.close()
    certificateFacade.importCertificates(file, companyId)

    FileResponse(-1, "Certificate", file.getName, "")
  }
}
