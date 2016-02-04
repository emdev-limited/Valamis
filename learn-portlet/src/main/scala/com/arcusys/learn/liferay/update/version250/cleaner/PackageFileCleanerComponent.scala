package com.arcusys.learn.liferay.update.version250.cleaner

import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.FileTableComponent
import com.arcusys.valamis.lesson.service.ValamisPackageService

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend
import scala.util.matching.Regex

trait PackageFileCleanerComponent extends FileTableComponent{
  protected val driver: JdbcProfile
  protected val valamisPackageService: ValamisPackageService

  import driver.simple._

  private def getIdHelper(r: Regex)(path: String) =
    r
      .findFirstMatchIn(path)
      .map(m => m.group(1).toLong)

  private val logoRegexp = "files/package_logo_(\\d+)/".r
  private val getIdFromLogoFileName = getIdHelper(logoRegexp) _
  private val logoPattern = "files/package_logo_%"

  protected def cleanPackageLogos()(implicit session: JdbcBackend#Session): Unit = {
    val packageLogos =
      files
        .map(_.filename)
        .filter(_.like(logoPattern))
        .run

    packageLogos.foreach { path =>
      val id = getIdFromLogoFileName(path).get
      val pack = try {
        Some(valamisPackageService.getPackage(id))
      } catch {
        case e: EntityNotFoundException => None
        case e => throw e
      }

      if(pack.isEmpty)
        files
          .filter(_.filename === path)
          .delete
    }
  }


  private val contentRegexp = "data/(\\d+)/".r
  private val getIdFromContentFileName = getIdHelper(contentRegexp) _
  private val contentPattern = "data/%"

  protected def cleanPackageContent()(implicit session: JdbcBackend#Session): Unit = {
    val packageContentFiles =
      files
        .map(_.filename)
        .filter(_.like(contentPattern))
        .run

    packageContentFiles.foreach { path =>
      val id = getIdFromContentFileName(path).get
      val pack = try {
        Some(valamisPackageService.getPackage(id))
      } catch {
        case e: EntityNotFoundException => None
        case e => throw e
      }

      if(pack.isEmpty)
        files
          .filter(_.filename === path)
          .delete
    }
  }
}
