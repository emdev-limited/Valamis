package com.arcusys.valamis.util.export

import java.io.File

import com.arcusys.valamis.util.serialization.JsonHelper._
import com.arcusys.valamis.util.{FileSystemUtil, ZipUtil}
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

trait ImportProcessor[T] {
  protected def importItems(items: List[T],
                            courseId: Long,
                            tempDirectory: File,
                            userId: Long,
                            data: String)

  def importItems(file: File,
                  courseId: Long,
                  userId: Long = -1,
                  fileType:String="")
                 (implicit ev: Manifest[List[T]], fs: Formats = DefaultFormats): Unit = {
    val tempDirectory = FileSystemUtil.getTempDirectory("Import")
    val exportFile = "export.json"
    ZipUtil.unzip(tempDirectory, file)

    val data = FileSystemUtil.getTextFileContent(new File(tempDirectory.getPath, exportFile))
    val items = Try(data.parseTo[List[T]]) match {
      case Success(d) => d
      case Failure(e) => throw e
    }

    importItems(items, courseId, tempDirectory, userId, data)

    FileSystemUtil.deleteFile(file)
    FileSystemUtil.deleteFile(tempDirectory)
  }
}