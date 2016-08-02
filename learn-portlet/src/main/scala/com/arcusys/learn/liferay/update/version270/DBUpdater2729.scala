package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.slide.SlideTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2729(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlideTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 2729

  def this() = this(Configuration)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>

      val textAndImageSlideId = slides
        .filter(x => x.isTemplate === true && x.title === "Text and image")
        .map(_.id)
        .firstOption


      textAndImageSlideId.foreach { id =>
        val textAndImageSlideMapper = slideElements
          .filter(x => x.slideId === id)
          .map(el => el.zIndex -> el.id).toMap

        textAndImageSlideMapper.get("1")
          .foreach { elementId =>
            updateContent(elementId, """<h1><span style="font-size:3em">Page header</span></h1>""")
          }
      }

      val textSlideId = slides
        .filter(x => x.isTemplate === true && x.title === "Text only")
        .map(_.id)
        .firstOption

      textSlideId.foreach { id =>
        val textSlideMapper = slideElements
          .filter(x => x.slideId === id)
          .map(el => el.zIndex -> el.id).toMap

        textSlideMapper.get("1")
          .foreach { elementId =>
            updateContent(elementId, """<h2><span style="font-size:3em">Page header</span></h2>""")
          }
      }

      val titleSlideId = slides
        .filter(x => x.isTemplate === true && x.title === "Title and subtitle")
        .map(_.id)
        .firstOption

      titleSlideId.foreach { id =>
        val titleSlideMapper = slideElements
          .filter(x => x.slideId === id)
          .map(el => el.zIndex -> el.id).toMap

        titleSlideMapper.get("1")
          .foreach { elementId =>
            updateContent(elementId, """<h1><span style="font-size:3em">Page header</span></h1>""")
          }
        titleSlideMapper.get("2")
          .foreach { elementId =>
            updateContent(elementId, """<h6><span style="font-size:2em">Page subtitle</span></h6>""")
          }
      }

      val videoSlideId = slides
        .filter(x => x.isTemplate === true && x.title === "Video only")
        .map(_.id)
        .firstOption

      videoSlideId.foreach { id =>
        val videoSlideMapper = slideElements
          .filter(x => x.slideId === id)
          .map(el => el.zIndex -> el.id).toMap

        videoSlideMapper.get("1")
          .foreach { elementId =>
            updateContent(elementId, """<h2><span style="font-size:3em">Page header</span></h2>""")
          }
      }

      val lessonSummarySlideId = slides
        .filter(x => x.isTemplate === true && x.isLessonSummary === true)
        .map(_.id)
        .firstOption

      lessonSummarySlideId.foreach { id =>
        val lessonSummarySlideMapper = slideElements
          .filter(x => x.slideId === id)
          .map(el => el.zIndex -> el.id).toMap

        lessonSummarySlideMapper.get("1")
          .foreach { elementId =>
            updateContent(elementId, """<h1><span style="font-size:3em">Lesson summary</span></h1>""")
          }
      }

    }
  }

  private def updateContent(id: Long, content: String) = {
    db.withTransaction { implicit session =>
      slideElements.filter(_.id === id).map(_.content).update(content)
    }
  }

  private def updateProperties(slideElementId: Long,
                               top: String,
                               left: String,
                               width: String,
                               height: String) = {
    db.withTransaction { implicit session =>
      val deviceId = 1L //default device(desktop)
    val property = slideElementProperties.filter(_.slideElementId === slideElementId).firstOption
      if (property.nonEmpty) {
        slideElementProperties.filter(x => x.deviceId === deviceId && x.slideElementId === slideElementId && x.key === "width")
          .map(_.value)
          .update(width)
        slideElementProperties.filter(x => x.deviceId === deviceId && x.slideElementId === slideElementId && x.key === "height")
          .map(_.value)
          .update(height)
        slideElementProperties.filter(x => x.deviceId === deviceId && x.slideElementId === slideElementId && x.key === "top")
          .map(_.value)
          .update(top)
        slideElementProperties.filter(x => x.deviceId === deviceId && x.slideElementId === slideElementId && x.key === "left")
          .map(_.value)
          .update(left)
      }

    }
  }
}
