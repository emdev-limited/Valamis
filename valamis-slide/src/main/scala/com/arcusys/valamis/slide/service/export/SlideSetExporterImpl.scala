package com.arcusys.valamis.slide.service.export

import java.io.FileInputStream

import com.arcusys.valamis.slide.model.{Slide, SlideSet}
import com.arcusys.valamis.slide.service.SlideService
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.util.ZipBuilder
import com.arcusys.valamis.util.export.ExportProcessor
import com.arcusys.valamis.util.serialization.DateTimeSerializer
import org.json4s.{DefaultFormats, Formats}

trait SlideSetExporter {
  def exportItems(items: Seq[SlideSet])(implicit fs: Formats = DefaultFormats): FileInputStream
}

abstract class SlideSetExporterImpl
  extends SlideSetExportUtils
  with ExportProcessor[SlideSet, ExportFormat]
  with SlideSetExporter {

  def slideService: SlideService
  implicit val jsonFormats = DefaultFormats + DateTimeSerializer

  protected def exportItemsImpl(zip: ZipBuilder, slideSets: Seq[SlideSet]) = {
    require(slideSets.length == 1)

    val slideSet = slideSets.head
    val slides = slideService.getSlides(slideSet.id)
    val questions = getQuestions(slides)
    val plaintexts = getPlainTexts(slides)
    val categories = (questions.flatMap(_._1.categoryId) ++ plaintexts.flatMap(_.categoryId)).distinct
      .flatMap(categoryService.getByID)

    val questionResponse = questions.map((QuestionExternalFormat.exportQuestion _).tupled)
    val plaintextResponse = plaintexts.map(QuestionExternalFormat.exportPlainText)
    val categoryResponse = categories.map(QuestionExternalFormat.exportCategory)

    val images = getSlidesFiles(slides)
    val logo = slideSet.logo
      .map(logo => SlideSetHelper.filePathPrefix(slideSet, slidesVersion) -> logo)
      .map(getPathAndInputStream)

    omitFileDuplicates(if (logo.isEmpty) images else logo.get +: images) foreach {
      case (path, inputStream) =>
        zip.addFile(inputStream, path)
    }

    Seq(ExportFormat(slidesVersion, questionResponse, plaintextResponse, categoryResponse, slideSet.toExportModel(slides)))
  }

  implicit class SlideModelExtension(val slideSet: SlideSet) {
    def toExportModel(slides: Seq[Slide]): SlideSetExportModel = {
      SlideSetExportModel(
        id = slideSet.id,
        title = slideSet.title,
        description = slideSet.description,
        courseId = slideSet.courseId,
        logo = slideSet.logo,
        slides = slides,
        isTemplate = slideSet.isTemplate,
        isSelectedContinuity = slideSet.isSelectedContinuity,
        themeId = slideSet.themeId,
        duration = slideSet.duration,
        scoreLimit = slideSet.scoreLimit,
        playerTitle = slideSet.playerTitle,
        slidesCount = Some(slides.size),
        topDownNavigation = slideSet.topDownNavigation,
        activityId = slideSet.activityId,
        status = slideSet.status,
        version = slideSet.version,
        modifiedDate = slideSet.modifiedDate,
        oneAnswerAttempt = slideSet.oneAnswerAttempt,
        tags = Nil,
        requiredReview = Some(slideSet.requiredReview)
      )
    }
  }
}