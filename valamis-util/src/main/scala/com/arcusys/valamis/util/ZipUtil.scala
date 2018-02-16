package com.arcusys.valamis.util

import java.io.File
import java.util.zip.ZipFile

import scala.collection.JavaConverters._

object ZipUtil {

  def zipContains(filename: String, zipFile: File): Boolean = {
    try {
      val zip = new ZipFile(zipFile)
      val entries = zip.entries

      while (entries.hasMoreElements) {
        val entry = entries.nextElement
        if (entry.getName.endsWith(filename)) {
          return true
        }
      }
      zip.close()
      false
    } catch {
      case e: Exception => throw new Exception(e.toString)
    }
  }

  def findInZip(zipFile: File, condition: String => Boolean): Option[String] = {
    val zip = new ZipFile(zipFile)
    try {
      zip.entries.asScala
        .find(e => condition(e.getName))
        .map(e => e.getName)
    }
    finally {
      zip.close()
    }
  }

  def unzipFile(filename: String, targetDirectory: File, zipFile: File) {
    targetDirectory.mkdirs
    val zip = new ZipFile(zipFile)
    try {
      zip.entries.asScala
        .find { e => e.getName.endsWith(filename) }
        .foreach { e =>
          val inputStream = zip.getInputStream(e)
          StreamUtil.writeToFile(inputStream, new File(targetDirectory, filename))
          inputStream.close()
        }
    }
    finally {
      zip.close()
    }
  }

  def unzip(targetDirectory: File, zipFile: File) {
    try {
      targetDirectory.mkdirs
      val zip = new ZipFile(zipFile, ZipFile.OPEN_READ)
      val entries = zip.entries

      while (entries.hasMoreElements) {
        val entry = entries.nextElement
        if (entry.isDirectory) {
          new File(targetDirectory, entry.getName).mkdirs
        } else {
          // if zip-file doesn't contains directory structure
          val rootDirectories = extractRootDirectories(entry.getName)
          for (i <- 1 to rootDirectories.length) {
            val currentDir = new File(targetDirectory, rootDirectories.splitAt(i)._1.mkString("/"))
            if (!currentDir.exists) currentDir.mkdirs
          }

          val inputStream = zip.getInputStream(entry)
          StreamUtil.writeToFile(inputStream, new File(targetDirectory, entry.getName))
          inputStream.close()
        }
      }
      zip.close()
    } catch {
      case _: Throwable => //throw new Exception("Can't unzip")
    }
  }

  private def extractRootDirectories(filename: String) = filename.split("/").dropRight(1) // split path to directory list and drop filename from list

}
