package com.arcusys.valamis.reports.service

import com.arcusys.valamis.reports.model._
import com.arcusys.valamis.util.FileSystemUtil
import org.joda.time.{DateTime, Days}
import com.arcusys.valamis.util.serialization.JsonHelper
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.DateTimeSerializer
import java.io.{BufferedWriter, File, FileFilter, FileWriter}

/**
  * Created by amikhailov on 31/05/2017.
  */
trait ReportExportSupport extends ReportService{
  implicit val formats: Formats = DefaultFormats + DateTimeSerializer

  private val endOfLine = "\r\n"

  private val tempDir = "reportexport"

  override def saveUsersAsCsv(data: Seq[MostActiveUsers]): File = {
    val file = getReportFile(formatFilename("users", "csv"))
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // output header
      writer.write(s"ID,Name,Count Certificates,Count Lessons$endOfLine")

      // output body
      data.foreach(item => {
        writer.write("%d,\"%s\",%d,%d%s".format(
          item.id,
          escape(item.name),
          item.countCertificates,
          item.countLessons,
          endOfLine
        ))
      })
    } finally {
      writer.close()
    }

    file
  }

  override def saveUsersAsJson(data: Seq[MostActiveUsers]): File = {
    val file = getReportFile(formatFilename("users", "json"))
    val writer = new BufferedWriter(new FileWriter(file))

    val json = JsonHelper.toJson {
      data.map(item => Map(
        "id" -> item.id,
        "name" -> item.name,
        "countCertificates" -> item.countCertificates,
        "countLessons" -> item.countLessons
        )
      )
    }

    try {
      writer.write(json)
    } finally {
      writer.close()
    }

    file
  }


  override def saveAverageGradesAsCsv(data: Seq[AveragePassingGrades]): File = {
    val file = getReportFile(formatFilename("average", "csv"))
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // output header
      writer.write(s"Title,Avegage grade%$endOfLine")

      // output body
      data.foreach(item => {
        writer.write("\"%s\", %d%s".format(
          escape(item.lessonTitle),
          Math.round(item.grade * 100),
          endOfLine
        ))
      })
    } finally {
      writer.close()
    }

    file
  }


  override def saveAverageGradesAsJson(data: Seq[AveragePassingGrades]): File = {
    val file = getReportFile(formatFilename("average", "json"))
    val writer = new BufferedWriter(new FileWriter(file))

    val json = JsonHelper.toJson {
      data.map(item => Map(
        "title" -> item.lessonTitle,
        "averageGrade" -> Math.round(item.grade * 100)
      ))
    }

    try {
      writer.write(json)
    } finally {
      writer.close()
    }

    file
  }

  override def saveLessonsAsCsv(data: Seq[TopLessonWithPopularity]): File = {
    val file = getReportFile(formatFilename("lessons", "csv"))
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // output header
      writer.write(s"ID,Title,Popularity,Count Completed$endOfLine")

      // output body
      data.foreach(item => {
        writer.write("%d,\"%s\",%.2f%%,%d%s".format(
          item.topLesson.id,
          escape(item.topLesson.title),
          item.popularity,
          item.topLesson.countCompleted,
          endOfLine
        ))
      })
    } finally {
      writer.close()
    }

    file
  }

  override def saveLessonsAsJson(data: Seq[TopLessonWithPopularity]): File = {
    val file = getReportFile(formatFilename("lessons", "json"))
    val writer = new BufferedWriter(new FileWriter(file))

    val json = JsonHelper.toJson {
      data.map(item => Map(
        "id" -> item.topLesson.id,
        "title" -> item.topLesson.title,
        "popularity" -> item.popularity,
        "countCompleted" -> item.topLesson.countCompleted)
      )
    }

    try {
      writer.write(json)
    } finally {
      writer.close()
    }

    file
  }

  override def saveCertificatesAsCsv(data: Seq[CertificateReportRow]): File = {
    val file = getReportFile(formatFilename("certificates", "csv"))
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // output header
      writer.write(s"Date,Count Achieved,Count In Progress$endOfLine")
      // output body
      data.foreach(item => {
        writer.write("\"%s\",%d,%d%s".format(
          item.date.toString("YYYY-MM-dd"),
          item.countAchieved,
          item.countInProgress,
          endOfLine
        ))
      })
    } finally {
      writer.close()
    }

    file
  }

  override def saveCertificatesAsJson(data: Seq[CertificateReportRow]): File = {
    val file = getReportFile(formatFilename("certificates", "json"))
    val writer = new BufferedWriter(new FileWriter(file))

    val json = JsonHelper.toJson(data)

    try {
      writer.write(json)
    } finally {
      writer.close()
    }

    file
  }

  override def saveAttemptedLessonsAsCsv(data: Seq[AttemptedLessonsRow]): File = {
    val file = getReportFile(formatFilename("attempted", "csv"))
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // output header
      writer.write(s"ID,User,Count Attempted,Count Finished$endOfLine")
      // output body
      data.foreach(item => {
        writer.write("%d,\"%s\",%d,%d%s".format(
          item.id,
          item.name,
          item.countAttempted,
          item.countFinished,
          endOfLine
        ))
      })
    } finally {
      writer.close()
    }

    file
  }

  override def saveAttemptedLessonsAsJson(data: Seq[AttemptedLessonsRow]): File = {
    val file = getReportFile(formatFilename("attempted", "json"))
    val writer = new BufferedWriter(new FileWriter(file))

    val json = JsonHelper.toJson(data)

    try {
      writer.write(json)
    } finally {
      writer.close()
    }

    file
  }

  override def getReportFile(filename: String): File = {
    new File(getTempDirectory() + filename)
  }

  override def getMimeType(filename: String): String = {
    val contentType = getExtension(filename) match {
      case "json" => "application/json"
      case "csv" => "text/csv"
      case _ => FileSystemUtil.getMimeType(filename)
    }
    contentType
  }

  override def cleanReportDir(expiredInSeconds: Int): Unit = {
    val dir = new File(getTempDirectory())
    val filter = new FileFilter {
      override def accept(pathname: File): Boolean = List("json", "csv").contains(getExtension(pathname.getName))
    }

    dir.listFiles(filter).foreach(file => {
      if (DateTime.now().getMillis - (file.lastModified()) > expiredInSeconds * 1000) file.delete()
    })
  }

  private def escape(str: String) = {
    str.replace("\"", "\"\"")
  }

  private def getTempDirectory(): String = {
    val dir = new File(FileSystemUtil.getValamisTempDirectory, tempDir)
    dir.mkdirs()
    dir.getPath + File.separator
  }

  private def formatFilename(reportType: String, reportFormat: String): String = {
    "%s_report_%s.%s".format(reportType, DateTime.now.toString("YYYY-MM-dd_HH-mm-ss"), reportFormat)
  }

  private def getExtension(filename: String) = {
    filename.split('.').last.toLowerCase
  }
}
