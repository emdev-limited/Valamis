package com.arcusys.valamis.web.servlet.file

import java.io._

import com.arcusys.json.JsonHelper
import com.arcusys.valamis.certificate.service.export.CertificateImportProcessor
import com.arcusys.valamis.content.export.{QuestionImportProcessor, QuestionMoodleImportProcessor}
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.service.export.PackageImportProcessor
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateFacadeContract
import com.arcusys.valamis.web.servlet.file.request.FileRequest
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class FileFacade(implicit val bindingModule: BindingModule) extends FileFacadeContract with Injectable {

  private val fileService = inject[FileService]
  private val certificateFacade = inject[CertificateFacadeContract]

  def saveFile(folder: String, name: String, content: Array[Byte]): FileResponse = {
    fileService.setFileContent(folder, name, content)
    new FileResponse(0, "", name, "")
  }

  def getFileContent(folder: String, name: String): Array[Byte] = {
    fileService.getFileContent(folder, name)
  }

  def updatePackage(id: Int, title: Option[String], summary: Option[String]) = {
    throw new NotImplementedError
  }

  def attachImageToPackage(packageId: Int, imageId: Int) = throw new NotImplementedError

  def getPackages(skip: Int,
    take: Int,
    filter: String,
    sortAZ: Boolean): Seq[FileResponse] = throw new NotImplementedError

  def getScormPackages(skip: Int,
    take: Int,
    filter: String,
    sortAZ: Boolean): Seq[FileResponse] = throw new NotImplementedError

  def getTincanPackages(skip: Int,
    take: Int,
    filter: String,
    sortAZ: Boolean): Seq[FileResponse] = throw new NotImplementedError

  def packageCount(skip: Int,
    take: Int,
    filter: String): Int = throw new NotImplementedError

  def scormPackageCount(skip: Int,
    take: Int,
    filter: String): Int = throw new NotImplementedError

  def tincanPackageCount(skip: Int,
    take: Int,
    filter: String): Int = throw new NotImplementedError

  def getScormPackage(scormPackageId: Int): FileResponse = throw new NotImplementedError

  def getTincanPackage(tincanPackageId: Int): FileResponse = throw new NotImplementedError

  override def importQuestions(courseId: Int, stream: InputStream): FileResponse = {
    val file = FileSystemUtil.streamToTempFile(stream, "Import", FileRequest.ExportExtension)
    stream.close()
    new QuestionImportProcessor().importItems(file, courseId)

    FileResponse(-1, "Question", file.getName, "")
  }

  override def importMoodleQuestions(courseId: Int, stream: InputStream): FileResponse = {
    var file:File = null
    try {
      file = FileSystemUtil.streamToTempFile(stream, "Import", "xml")
      stream.close()
      val res = new QuestionMoodleImportProcessor(courseId).importItems(file)
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

    new CertificateImportProcessor().importItems(file, companyId)

    FileResponse(-1, "Certificate", file.getName, "")
  }
}
