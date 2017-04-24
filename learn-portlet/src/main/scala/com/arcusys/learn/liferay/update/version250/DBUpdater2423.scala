package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.liferay.portal.kernel.log.LogFactoryUtil

class DBUpdater2423(dbInfo: SlickDBInfo) extends LUpgradeProcess with SlideTableComponent {
  def this() = this(Configuration.inject[SlickDBInfo](None))

  val logger = LogFactoryHelper.getLog(getClass)
  implicit val bindingModule = Configuration

  override def getThreshold = 2423

  lazy val driver = dbInfo.slickDriver
  lazy val db = dbInfo.databaseDef
  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val lessonSummarySlideId = slides.filter{ slide =>
        slide.isTemplate === true && slide.isLessonSummary === true
      }.first.id

      var elements = Seq[SlideElement]()
      lessonSummarySlideId.foreach {id =>
        elements = elements :+  createSlideElementEntity("205", "80", "800", "400", "3",
          "<div style=\"text-align:left\"><span style=\"font-size:20px\" id=\"lesson-summary-table\">Summary information of this lesson will be placed here</span></div>",
          "text", id)
      }
      slideElements ++= elements
    }
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