package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.slide.SlideTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2711(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlideTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 2711

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
          .foreach(createProperties(_, "25", "80", "800", "80"))
        textAndImageSlideMapper.get("2")
          .foreach(createProperties(_, "160", "60", "320", "490"))
        textAndImageSlideMapper.get("3")
          .foreach(createProperties(_, "160", "420", "480", "490"))
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
          .foreach(createProperties(_, "25", "80", "800", "80"))
        textSlideMapper.get("2")
          .foreach(createProperties(_, "160", "80", "800", "490"))
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
          .foreach(createProperties(_, "160", "80", "800", "80"))
        titleSlideMapper.get("2")
          .foreach(createProperties(_, "240", "80", "800", "80"))
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
          .foreach(createProperties(_, "25", "80", "800", "80"))
        videoSlideMapper.get("2")
          .foreach(createProperties(_, "160", "80", "800", "490"))
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
          .foreach(createProperties(_, "25", "80", "800", "80"))
        lessonSummarySlideMapper.get("3")
          .foreach(createProperties(_, "205", "80", "800", "400"))
      }

    }
  }

  private def createProperties(slideElementId: Long,
                               top: String,
                               left: String,
                               width: String,
                               height: String) = {
    db.withTransaction { implicit session =>
      val deviceId = 1 //default device(desktop)
      val property = slideElementProperties.filter(_.slideElementId === slideElementId).firstOption
      if (property.isEmpty){
        val properties =
          SlideElementProperty(slideElementId, deviceId, "width", width) ::
            SlideElementProperty(slideElementId, deviceId, "height", height) ::
            SlideElementProperty(slideElementId, deviceId, "top", top) ::
            SlideElementProperty(slideElementId, deviceId, "left", left) ::
            Nil

        slideElementProperties ++= properties
      }
    }
  }
}
