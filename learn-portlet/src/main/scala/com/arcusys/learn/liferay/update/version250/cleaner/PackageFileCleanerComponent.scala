package com.arcusys.learn.liferay.update.version250.cleaner

import com.arcusys.valamis.persistence.impl.file.FileTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.jdbc.{JdbcBackend, StaticQuery}
import scala.util.matching.Regex

trait PackageFileCleanerComponent extends FileTableComponent { self: SlickProfile =>
  protected val db: JdbcBackend#DatabaseDef

  import driver.simple._

  private def getIdHelper(r: Regex)(path: String) =
    r
      .findFirstMatchIn(path)
      .map(m => m.group(1).toLong)

  private val logoRegexp = "files/package_logo_(\\d+)/".r
  private val getIdFromLogoFileName = getIdHelper(logoRegexp) _
  private val logoPattern = "files/package_logo_%"

  private lazy val packageIds = {
    db.withSession { implicit s =>
      val scormIds = StaticQuery.queryNA[(Long, Option[String])]("select id_ from learn_lfpackage").list
      val tincanIds = StaticQuery.queryNA[(Long, Option[String])]("select id_ from learn_lftincanpackage").list
      scormIds ++ tincanIds
    }
  }

  protected def cleanPackageLogos()(implicit session: JdbcBackend#Session): Unit = {
    val packageLogos =
      files
        .map(_.filename)
        .filter(_.like(logoPattern))
        .run

    packageLogos.foreach { path =>
      val id = getIdFromLogoFileName(path).get
      val hasLesson = packageIds.contains(id)

      if (!hasLesson)
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
      val hasLesson = packageIds.contains(id)

      if(!hasLesson)
        files
          .filter(_.filename === path)
          .delete
    }
  }
}
