package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version260.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2706(dbInfo: SlickDBInfo) extends LUpgradeProcess with SlideTableComponent {

  import driver.simple._

  val db = dbInfo.databaseDef
  val driver = dbInfo.slickProfile

  def this() = this(Configuration.inject[SlickDBInfo](None))

  override def getThreshold = 2706

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>

      val slideId = slides
        .filter(x => x.isTemplate === true && x.isLessonSummary === true)
        .map(_.id)
        .firstOption

      val zIndexToContent = Map(
        "1" -> "<div><strong><span style=\"font-size:1.5em; font-weight:normal;\">Lesson Summary</span></strong></div>",
        "3" -> "<div style=\"left: 25%; top: 45%; position: absolute;\"><span id=\"lesson-summary-table\">Summary information</span></div>"
      )

      for {
        id <- slideId
        (zIndex, content) <- zIndexToContent
      } {
        slideElements
          .filter(x => x.slideId === id && x.zIndex === zIndex)
          .map(_.content)
          .update(content)
      }
    }
  }
}