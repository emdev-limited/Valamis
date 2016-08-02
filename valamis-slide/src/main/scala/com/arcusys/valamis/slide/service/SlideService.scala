package com.arcusys.valamis.slide.service

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lrs.service.util.{TinCanVerb, TinCanVerbs}
import com.arcusys.valamis.slide.convert.{PDFProcessor, PresentationProcessor}
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.SlideModelConverters._
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.slide.storage.{SlidePropertyRepository, SlideRepository, SlideSetRepository}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

abstract class SlideService(implicit val bindingModule: BindingModule)
  extends Injectable
  with SlideServiceContract {

  private val slideRepository = inject[SlideRepository]
  private val slideSetRepository = inject[SlideSetRepository]
  private val slideElementService = inject[SlideElementServiceContract]
  private lazy val fileService = inject[FileService]
  def presentationProcessor: PresentationProcessor
  def pdfProcessor: PDFProcessor
  private lazy val slideThemeService = inject[SlideThemeServiceContract]
  private val slidePropertyRepository = inject[SlidePropertyRepository]

  private implicit def convertToModel(from: SlideEntity) =
    slideModelConversion(from, slideElementService.getBySlideId(from.id.get))

  private implicit def convertToModelList(from: List[SlideEntity]) = from.map(convertToModel)
  private implicit def convertToModelOption(from: Option[SlideEntity]) = from.map(convertToModel)

  override def getAll(isTemplate: Option[Boolean]): List[SlideModel] = slideRepository.getAll(isTemplate)

  override def getById(id: Long): Option[SlideModel] = slideRepository.getById(id)

  override def getBySlideSetId(slideSetId: Long, isTemplate: Option[Boolean] = None): List[SlideModel] =
    slideRepository.getBySlideSetId(slideSetId, isTemplate)

  override def getLogo(slideSetId: Long): Option[Array[Byte]] = {
    def getLogo(slide: SlideModel) = {
      slide.bgImage
        .map(bgImage => logoPath(slide, bgImage))
        .flatMap(fileService.getFileContentOption)
        .get
    }

    getById(slideSetId)
      .map(getLogo)
  }

  override def setLogo(slideId: Long, name: String, content: Array[Byte]): Unit = {
    getById(slideId).map { slide =>
      fileService.setFileContent(
        folder = filePathPrefix(slide),
        name = name,
        content = content,
        deleteFolder = false
      )
      update(slide.copy(
        bgImage = Some(name)
        )
      )
    }
  }

  override def getTinCanVerbs: List[TinCanVerb] = TinCanVerbs.all.map(x => TinCanVerb(TinCanVerbs.getVerbURI(x), x))

  override def countBySlideSet(slideSetId: Long, isTemplate: Option[Boolean]): Long =
    slideRepository.countBySlideSetId(slideSetId, isTemplate)

  override def delete(id: Long): Unit = {
    getById(id)
      .flatMap(entity => Some(filePathPrefix(entity)))
      .foreach(fileService.deleteByPrefix)

    slideRepository.delete(id)
  }

  override def create(slideModel: SlideModel): SlideModel = {
    val createdSlide = slideRepository.create(slideModel)
    slidePropertyRepository.create(slideModel, createdSlide.id.get)
    createdSlide
  }

  override def update(slideModel: SlideModel): SlideModel = {
    val updatedSlide = slideRepository.update(slideModel)
    slidePropertyRepository.replace(slideModel, updatedSlide.id.get)
    updatedSlide
  }

  override def clone(slideId: Long,
                     leftSlideId: Option[Long],
                     topSlideId: Option[Long],
                     bgImage: Option[String],
                     slideSetId: Long,
                     isTemplate: Boolean,
                     isLessonSummary: Boolean,
                     fromTemplate: Boolean): Option[SlideModel] = {
    val newSlideSetId =
      if(slideSetId > 0)
        slideSetId
      else {
        //Now it returns with count
        val set = slideSetRepository.getTemplatesWithCount(0L).head._1
        set.id.get
      }

    val newLeftSlideId = if(slideSetId > 0) leftSlideId else None
    val newTopSlideId  = if(slideSetId > 0) topSlideId  else None
    getById(slideId).map { slide =>
      val clonedSlide =
        create(
          SlideModel(
            None,
            slide.title,
            slide.bgColor,
            if (fromTemplate) bgImage else slide.bgImage,
            slide.font,
            slide.questionFont,
            slide.answerFont,
            slide.answerBg,
            slide.duration,
            newLeftSlideId,
            newTopSlideId,
            List(),
            newSlideSetId,
            slide.statementVerb,
            slide.statementObject,
            slide.statementCategoryId,
            isTemplate,
            isLessonSummary,
            slide.playerTitle,
            slide.properties)
        )

      if (!fromTemplate) {
        slide.bgImage.foreach { bgImage =>
          if (!bgImage.isEmpty && !bgImage.contains("/")) {
            val fileName = bgImage.takeWhile(_ != ' ')
            fileService.copyFile(
              filePathPrefix(slide),
              fileName,
              filePathPrefix(clonedSlide),
              fileName,
              false
            )
          }
        }
      }
      clonedSlide
    }
  }

  override def parsePDF(content: Array[Byte]):  List[String] =
    pdfProcessor.parsePDF(content)

  override def parsePPTX(content: Array[Byte], fileName: String):  List[String]=
    presentationProcessor.parsePPTX(content, fileName)

  override def copyFileFromTheme(slideId: Long, themeId: Long): Option[SlideModel] = {
    val themeModel = slideThemeService.getById(themeId)
    getById(slideId).map { slideModel =>
      themeModel.bgImage.foreach { bgImage =>
        val image = bgImage.takeWhile(_ != ' ')
        fileService.copyFile(
          filePathPrefix(themeModel),
          image,
          filePathPrefix(slideModel),
          image,
          false
        )
      }
      slideModel
    }
  }

  private def getProperties(slideId: Long): Seq[SlideProperties] = {
    val propertyList = slidePropertyRepository.getBySlideId(slideId)
    val grouped = propertyList.groupBy(_.deviceId)
    grouped.map(group =>
      SlideProperties(
        group._1,
        group._2
      )
    ).toSeq
  }

  implicit def slideModelConversion(entity: SlideEntity, slideElements: List[SlideElementModel]): SlideModel = {
    val properties = getProperties(entity.id.get)
    SlideModel(
      entity.id,
      entity.title,
      entity.bgColor,
      entity.bgImage,
      entity.font,
      entity.questionFont,
      entity.answerFont,
      entity.answerBg,
      entity.duration,
      entity.leftSlideId,
      entity.topSlideId,
      slideElements,
      entity.slideSetId,
      entity.statementVerb,
      entity.statementObject,
      entity.statementCategoryId,
      entity.isTemplate,
      entity.isLessonSummary,
      entity.playerTitle,
      properties
    )
  }
}