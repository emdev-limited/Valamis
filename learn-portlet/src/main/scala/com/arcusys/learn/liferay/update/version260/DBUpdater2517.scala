package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent

class DBUpdater2517(dbInfo: SlickDBInfo) extends LUpgradeProcess with SlideTableComponent {
  override def getThreshold = 2517

  val db = dbInfo.databaseDef
  val driver = dbInfo.slickProfile
  import driver.simple._

  def this() = this(Configuration.inject[SlickDBInfo](None))

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>

      val textAndImageSlideId = slides.filter(x => x.title === "Text and image" && x.isTemplate === true).firstOption.flatMap(_.id)
      updateContent(textAndImageSlideId, "1", "<div><strong><span style=\"font-size:3em\">Page header</span></strong></div>")
      updateContent(textAndImageSlideId, "2", "<div style=\"text-align:left\"><span style=\"font-size:1.25em, font-weight: lighter\">Page text</span></div>")

      val textSlideId = slides.filter(x => x.title === "Text only" && x.isTemplate === true).firstOption.flatMap(_.id)
      updateContent(textSlideId, "1", "<div><span style=\"font-size:3em\">Page header</span></div>")
      updateContent(textSlideId, "2", "<div style=\"text-align:left\"><span style=\"font-size:1.25em\">Page text</span></div>")

      val titleSlideId = slides.filter(x => x.title === "Title and subtitle" && x.isTemplate === true).firstOption.flatMap(_.id)
      updateContent(titleSlideId, "1", "<div><strong><span style=\"font-size:3em\">Page header</span></strong></div>")
      updateContent(titleSlideId, "2", "<div><span style=\"font-size:1.25em\">Page subtitle</span></div>")


      val videoSlideId = slides.filter(x => x.title === "Video only" && x.isTemplate === true).firstOption.flatMap(_.id)
      updateContent(videoSlideId, "1", "<div><span style=\"font-size:3em\">Page header</span></div>")


      val lessonSummarySlideId = slides.filter(x => x.isTemplate === true && x.isLessonSummary === true).firstOption.flatMap(_.id)
      updateContent(lessonSummarySlideId, "1", "<div><strong><span style=\"font-size:1.5em, font-weight:normal;\">Lesson Summary</span></strong></div>")
      updateContent(lessonSummarySlideId, "3", "<div style=\"text-align:left\"><span style=\"font-size:1.25em\" id=\"lesson-summary-table\">Summary information of this lesson will be placed here</span></div>")
    }
  }

  private def updateContent(slideId: Option[Long], zIndex: String, content: String) =
    db.withSession { implicit session =>
      if (slideId.isDefined) {
        slideElements.filter(x => x.slideId === slideId && x.zIndex === zIndex).map(_.content).update(content)
      }
    }
}
