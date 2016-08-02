package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.liferay.portal.kernel.log.LogFactoryUtil

class DBUpdater2420(dbInfo: SlickDBInfo) extends LUpgradeProcess with SlideTableComponent{

  val logger = LogFactoryHelper.getLog(getClass)
  override def getThreshold = 2420

  lazy val driver = dbInfo.slickDriver
  lazy val db = dbInfo.databaseDef
  import driver.simple._

  def this() = this(Configuration.inject[SlickDBInfo](None))

  override def doUpgrade(): Unit = {
    db.withSession { implicit session =>
        val slideSetId = slideSets.filter(_.courseId === 0L).first.id
        val lessonSummarySlideId = slides returning slides.map(_.id) +=
          createSlideEntity("Lesson summary", "lesson-summary.png", slideSetId.get)

        slideElements returning slideElements.map(_.id) insert createSlideElementEntity("25",
          "80",
          "800",
          "80",
          "1",
          "<div><strong><span style=\"font-size:24px; font-weight:normal;\">Lesson Summary</span></strong></div>",
          "text",
          lessonSummarySlideId)
      }
  }

  private def createSlideEntity(title: String, bgImage: String, slideSetId: Long): Slide ={
    Slide(
      title = title,
      bgImage = Some(bgImage),
      slideSetId = slideSetId,
      isTemplate = true,
      isLessonSummary = true)
  }

  private def createSlideElementEntity(top: String,
                                       left: String,
                                       width: String,
                                       height: String,
                                       zIndex: String,
                                       content: String,
                                       slideEntityType: String,
                                       slideId: Long): SlideElement ={
    SlideElement(
      top = top,
      left = left,
      width = width,
      height = height,
      zIndex = zIndex,
      content = content,
      slideEntityType = slideEntityType,
      slideId = slideId)
  }
}