package com.arcusys.valamis.util.export

import java.io.FileInputStream

import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.util.{FileSystemUtil, ZipBuilder}
import org.json4s.{DefaultFormats, Formats}

trait ExportProcessor[T, R] {
  protected def exportItemsImpl(zip: ZipBuilder, items: Seq[T]): Seq[R]

  def exportItems(items: Seq[T])(implicit fs: Formats = DefaultFormats): FileInputStream = {
    val zipFile = FileSystemUtil.getTempFile("Export", "zip")
    val zip = new ZipBuilder(zipFile)
    val exportResponse = exportItemsImpl(zip, items)
    zip.addEntry("export.json", JsonHelper.toJson(exportResponse))
    zip.close()

    val stream = new FileInputStream(zipFile)

    FileSystemUtil.deleteLater(zipFile, stream)

    stream
  }
}
