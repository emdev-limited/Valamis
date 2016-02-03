package com.arcusys.learn.liferay.update.version250.cleaner

import com.arcusys.valamis.file.FileTableComponent
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend
import scala.util.matching.Regex

trait SlideFileCleanerComponent extends FileTableComponent with SlideTableComponent{
  protected val driver: JdbcProfile
  import driver.simple._

  private def getId(regex: Regex, path: String) =
    regex
      .findFirstMatchIn(path)
      .map(m => m.group(1).toLong)

  private val slideSetLogoRegexp = "files/slideset_logo_(\\d+)/.+".r
  private val slideSetLogoPattern = "files/slideset_logo_%"

  def cleanSlideSetLogos()(implicit session: JdbcBackend#Session): Unit = {
    val slideSetLogos =
      files
        .map(_.filename)
        .filter(_.like(slideSetLogoPattern))
        .run

    slideSetLogos.foreach { path =>
      val id = getId(slideSetLogoRegexp, path).get
      val slideSet = slideSets.filter(_.id === id).firstOption
      if(slideSet.isEmpty)
        files
          .filter(_.filename === path)
          .delete
    }
  }

  private val slideBGImagePattern = "files/slide_%"
  private val slideBGImageRegexp = "files/slide_(\\d+)/.+".r
  def cleanSlideBGImages()(implicit session: JdbcBackend#Session): Unit = {
    val slideBGImages =
      files
        .map(_.filename)
        .filter(_.like(slideBGImagePattern))
        .run
        .filter(slideBGImageRegexp.findFirstMatchIn(_).isDefined)

    slideBGImages.foreach { path =>
      val id = getId(slideBGImageRegexp, path).get
      val slide = slides.filter(_.id === id).firstOption
      if(slide.isEmpty)
        files
          .filter(_.filename === path)
          .delete
    }
  }
}
