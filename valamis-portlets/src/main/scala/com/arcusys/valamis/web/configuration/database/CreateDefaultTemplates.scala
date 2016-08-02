package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.slide.SlideTableComponent
import com.arcusys.valamis.slide.model._
import slick.jdbc.JdbcBackend

import scala.slick.driver._

class CreateDefaultTemplates(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
  extends SlideTableComponent
    with SlickProfile {

  import driver.simple._

  def create(): Unit = {
    val defaultTemplate = db.withSession { implicit s =>
      slides.filter(_.isTemplate === true).firstOption
    }

    if (defaultTemplate.isEmpty) createTemplates()
  }

  private def createTemplates() = {
    db.withTransaction { implicit session =>
      val slideSetId = slideSets.filter(_.courseId === 0L).first.id
      val textAndImageSlideId = slides returning slides.map(_.id) +=
        createSlideEntity("Text and image", "text-and-image.png", slideSetId.get, false)
      val textSlideId = slides returning slides.map(_.id) +=
        createSlideEntity("Text only", "text-only.png", slideSetId.get, false)
      val titleSlideId = slides returning slides.map(_.id) +=
        createSlideEntity("Title and subtitle", "title-and-subtitle.png", slideSetId.get, false)
      val videoSlideId = slides returning slides.map(_.id) +=
        createSlideEntity("Video only", "video-only.png", slideSetId.get, false)
      val lessonSummarySlideId = slides returning slides.map(_.id) +=
        createSlideEntity("Lesson summary", "lesson-summary.png", slideSetId.get, true)

      val elementHeaderId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h1><span style="font-size:3em">Page header</span></h1>""",
          "text",
          textAndImageSlideId)

      //TODO: remove it
      //without this SELECT next insert will fall
      slideElements.filter(_.id === elementHeaderId).firstOption

      createProperties(elementHeaderId, "68", "121", "781", "80")

      val elementTextId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          "<p style=\"text-align:left\">Page text</p>",
          "text",
          textAndImageSlideId)
      createProperties(elementTextId, "199", "95", "320", "469")

      val elementImageId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "3",
          "",
          "image",
          textAndImageSlideId)
      createProperties(elementImageId, "199", "451", "480", "469")

      val elementHeaderForTextSlideId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h2><span style="font-size:3em">Page header</span></h2>""",
          "text",
          textSlideId)

      createProperties(elementHeaderForTextSlideId, "68", "121", "781", "80")

      val elementTextForTextSlideId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          "<p style=\"text-align:left\">Page text</p>",
          "text",
          textSlideId)
      createProperties(elementTextForTextSlideId, "199", "121", "781", "469")

      val elementTitleId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h1><span style="font-size:3em">Page header</span></h1>""",
          "text",
          titleSlideId)
      createProperties(elementTitleId, "198", "121", "781", "80")

      val elementSubtitleId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          """<h6><span style="font-size:2em">Page subtitle</span></h6>""",
          "text",
          titleSlideId)
      createProperties(elementSubtitleId, "276", "121", "781", "80")

      val elementHeaderForVideoSlideId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h2><span style="font-size:3em">Page header</span></h2>""",
          "text",
          videoSlideId)
      createProperties(elementHeaderForVideoSlideId, "68", "121", "781", "80")

      val elementVideoId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          "",
          "video",
          videoSlideId)
      createProperties(elementVideoId, "199", "121", "781", "469")


      val elementLessonSummaryId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h1><span style="font-size:3em">Lesson summary</span></h1>""",
          "text",
          lessonSummarySlideId)
      createProperties(elementLessonSummaryId, "68", "121", "781", "80")


      val elementSummaryInfoId = slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "3",
          "<div style=\"left: 25%; top: 43%; position: absolute;\"><span id=\"lesson-summary-table\">Summary information</span></div>",
          "text",
          lessonSummarySlideId)
      createProperties(elementSummaryInfoId, "199", "121", "781", "390")
    }
  }


  private def createSlideEntity(title: String, bgImage: String, slideSetId: Long, isLessonSummary: Boolean): SlideEntity = {
    SlideEntity(
      title = title,
      bgImage = Some(bgImage),
      slideSetId = slideSetId,
      isTemplate = true,
      isLessonSummary = isLessonSummary)
  }

  private def createSlideElementEntity(zIndex: String,
                                       content: String,
                                       slideEntityType: String,
                                       slideId: Long): SlideElementEntity = {
    SlideElementEntity(
      zIndex = zIndex,
      content = content,
      slideEntityType = slideEntityType,
      slideId = slideId)
  }

  private def createProperties(slideElementId: Long,
                               top: String,
                               left: String,
                               width: String,
                               height: String)
                              (implicit session: Session): Unit = {
    val deviceId = 1 //default device(desktop)
    val properties = SlideElementPropertyEntity(slideElementId, deviceId, "width", width) ::
      SlideElementPropertyEntity(slideElementId, deviceId, "height", height) ::
      SlideElementPropertyEntity(slideElementId, deviceId, "top", top) ::
      SlideElementPropertyEntity(slideElementId, deviceId, "left", left) ::
      Nil

    slideElementProperties ++= properties
  }
}

