package com.arcusys.valamis.export

import java.io.File

import com.arcusys.valamis.util.serialization.JsonHelper._
import com.arcusys.valamis.util.{FileSystemUtil, ZipUtil}

trait ImportProcessor[T] {
  protected def importItems(items: List[T], courseId: Long, tempDirectory: File, userId: Long)

  def importItems(file: File, scopeId: Long, userId: Long = -1,fileType:String="")(implicit ev: Manifest[List[T]]): Unit = {
    val tempDirectory = FileSystemUtil.getTempDirectory("Import")
    val exportFile = "export.json"
    ZipUtil.unzip(tempDirectory, file)

    val data = FileSystemUtil.getTextFileContent(new File(tempDirectory.getPath, exportFile))
    val items = data.parseTo[List[T]]

    importItems(items, scopeId, tempDirectory, userId)

    FileSystemUtil.deleteFile(file)
    FileSystemUtil.deleteFile(tempDirectory)
  }
}

