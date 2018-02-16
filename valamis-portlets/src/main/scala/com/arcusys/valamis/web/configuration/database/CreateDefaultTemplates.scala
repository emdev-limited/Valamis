package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model._
import slick.dbio.Effect.Write
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.profile.FixedSqlAction
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class CreateDefaultTemplates(val driver: JdbcProfile, val db: JdbcBackend#DatabaseDef)
  extends SlideTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  def create(): Unit = execSync {
    for {
      count <- slides.filter(_.isTemplate === true).size.result
      _ <- if (count == 0) {
        createTemplates()
      }
      else {
        DBIO.successful()
      }
    } yield ()
  }

  private def createTemplates() = {
    for {
      set <- slideSets.filter(_.courseId === 0L).result.head
      textAndImageSlideId <- slides returning slides.map(_.id) +=
        createSlideEntity("Text and image", "text-and-image.png", set.id, isLessonSummary = false)
      textSlideId <- slides  returning slides.map(_.id) +=
        createSlideEntity("Text only", "text-only.png", set.id, isLessonSummary = false)
      titleSlideId <- slides returning slides.map(_.id) +=
        createSlideEntity("Title and subtitle", "title-and-subtitle.png", set.id, isLessonSummary = false)
      videoSlideId <- slides returning slides.map(_.id) +=
        createSlideEntity("Video only", "video-only.png", set.id, isLessonSummary = false)
      lessonSummarySlideId <- slides returning slides.map(_.id) +=
        createSlideEntity("Lesson summary", "lesson-summary.png", set.id, isLessonSummary = true)
      elementHeaderId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h1><span style="font-size:3em">Page header</span></h1>""",
          "text",
          textAndImageSlideId)
      // TODO: add after first element insert(need to check at Maxim)
      //slideElements.filter(_.id === elementHeaderId).firstOption
      _ <- createProperties(elementHeaderId, "68", "121", "781", "80")
      elementTextId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          "<p style=\"text-align:left\">Page text</p>",
          "text",
          textAndImageSlideId)
      _ <- createProperties(elementTextId, "199", "95", "320", "469")
      elementImageId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "3",
          "",
          "image",
          textAndImageSlideId)
      _ <- createProperties(elementImageId, "199", "451", "480", "469")
      elementHeaderForTextSlideId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h2><span style="font-size:3em">Page header</span></h2>""",
          "text",
          textSlideId)
      _ <- createProperties(elementHeaderForTextSlideId, "68", "121", "781", "80")
      elementTextForTextSlideId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          "<p style=\"text-align:left\">Page text</p>",
          "text",
          textSlideId)
      _ <- createProperties(elementTextForTextSlideId, "199", "121", "781", "469")
      elementTitleId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h1><span style="font-size:3em">Page header</span></h1>""",
          "text",
          titleSlideId)
      _ <- createProperties(elementTitleId, "198", "121", "781", "80")
      elementSubtitleId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          """<h6><span style="font-size:2em">Page subtitle</span></h6>""",
          "text",
          titleSlideId)
      _ <- createProperties(elementSubtitleId, "276", "121", "781", "80")
      elementHeaderForVideoSlideId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h2><span style="font-size:3em">Page header</span></h2>""",
          "text",
          videoSlideId)
      _ <- createProperties(elementHeaderForVideoSlideId, "68", "121", "781", "80")
      elementVideoId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "2",
          "",
          "video",
          videoSlideId)
      _ <- createProperties(elementVideoId, "199", "121", "781", "469")
      elementLessonSummaryId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "1",
          """<h1><span style="font-size:2em">Lesson summary</span></h1>""",
          "text",
          lessonSummarySlideId)
      _ <- createProperties(elementLessonSummaryId, "68", "121", "781", "80")
      elementSummaryInfoId <- slideElements returning slideElements.map(_.id) +=
        createSlideElementEntity(
          "3",
          "<span id=\"lesson-summary-table\"></span>",
          "text",
          lessonSummarySlideId)
      _ <- createProperties(elementSummaryInfoId, "199", "121", "781", "390")
    } yield ()
  }

  private def createSlideEntity(title: String, bgImage: String, slideSetId: Long, isLessonSummary: Boolean): Slide = {
    Slide(
      title = title,
      bgImage = Some(bgImage),
      slideSetId = slideSetId,
      isTemplate = true,
      isLessonSummary = isLessonSummary)
  }

  private def createSlideElementEntity(zIndex: String,
                                       content: String,
                                       slideEntityType: String,
                                       slideId: Long): SlideElement = {
    SlideElement(
      zIndex = zIndex,
      content = content,
      slideEntityType = slideEntityType,
      slideId = slideId)
  }

  private def createProperties(slideElementId: Long,
                               top: String,
                               left: String,
                               width: String,
                               height: String) = {
    val deviceId = 1 //default device(desktop)
    val properties = SlideElementPropertyEntity(slideElementId, deviceId, "width", width) ::
      SlideElementPropertyEntity(slideElementId, deviceId, "height", height) ::
      SlideElementPropertyEntity(slideElementId, deviceId, "top", top) ::
      SlideElementPropertyEntity(slideElementId, deviceId, "left", left) ::
      Nil

    slideElementProperties ++= properties
  }
}

