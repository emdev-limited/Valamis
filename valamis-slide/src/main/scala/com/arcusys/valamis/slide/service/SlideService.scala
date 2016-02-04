package com.arcusys.valamis.slide.service

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.imageio.ImageIO

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lrs.util.{TinCanVerb, TinCanVerbs}
import com.arcusys.valamis.slide.convert.{PDFProcessor, PresentationProcessor}
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.SlideModelConverters._
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.slide.storage.{SlidePropertyRepository, SlideRepository, SlideSetRepository}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class SlideService(implicit val bindingModule: BindingModule)
  extends Injectable
  with SlideServiceContract {

  private val slideRepository = inject[SlideRepository]
  private val slideSetRepository = inject[SlideSetRepository]
  private val slideElementService = inject[SlideElementServiceContract]
  private lazy val fileService = inject[FileService]
  private lazy val presentationProcessor = inject[PresentationProcessor]
  private lazy val pdfProcessor = inject[PDFProcessor]
  private lazy val slideThemeService = inject[SlideThemeServiceContract]
  private val slidePropertyRepository = inject[SlidePropertyRepository]

  private implicit def convertToModel(from: SlideEntity) =
    slideModelConversion(from, slideElementService.getBySlideId(from.id.get))

  private implicit def convertToModelList(from: List[SlideEntity]) = from.map(convertToModel)
  private implicit def convertToModelOption(from: Option[SlideEntity]) = from.map(convertToModel)

  override def getAll(isTemplate: Option[Boolean]) = slideRepository.getAll(isTemplate)

  override def getById(id: Long) = slideRepository.getById(id)

  override def getBySlideSetId(slideSetId: Long, isTemplate: Option[Boolean]) =
    slideRepository.getBySlideSetId(slideSetId, isTemplate)

  override def getLogo(slideSetId: Long) = {
    def getLogo(slide: SlideModel) = {
      slide.bgImage
        .map(bgImage => logoPath(slide, bgImage))
        .flatMap(fileService.getFileContentOption)
        .get
    }

    getById(slideSetId)
      .map(getLogo)
  }

  override def setLogo(slideId: Long, name: String, content: Array[Byte]) = {
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

  override def getTinCanVerbs() = TinCanVerbs.all.map(x => TinCanVerb(TinCanVerbs.getVerbURI(x), x))

  override def delete(id: Long) = {
    getById(id)
      .flatMap(entity => Some(filePathPrefix(entity)))
      .foreach(fileService.deleteByPrefix)

    slideRepository.delete(id)
  }

  override def create(slideModel: SlideModel) = {
    val createdSlide = slideRepository.create(slideModel)
    slideModel.properties.foreach(createProperties(createdSlide.id.get, _))
    createdSlide
  }

  override def update(slideModel: SlideModel) = {
    val updatedSlide = slideRepository.update(slideModel)
    slidePropertyRepository.delete(updatedSlide.id.get)
    slideModel.properties.foreach(createProperties(updatedSlide.id.get, _))
    updatedSlide
  }

  private def createProperties(slideId: Long, slideProperties: SlideProperties) = {
    val deviceId = slideProperties.deviceId
    slideProperties.properties.foreach(property =>
      slidePropertyRepository.create(
        SlidePropertyEntity(
          slideId,
          deviceId,
          property.key,
          property.value)
      )
    )
  }


  override def clone(slideId: Long,
                     leftSlideId: Option[Long],
                     topSlideId: Option[Long],
                     bgImage: Option[String],
                     slideSetId: Long,
                     isTemplate: Boolean,
                     isLessonSummary: Boolean,
                     fromTemplate: Boolean,
                     cloneElements: Boolean): Option[SlideModel] = {
    val newSlideSetId =
      if(slideSetId > 0)
        slideSetId
      else
        slideSetRepository.getSlideSets("", false, -1, -1, Some(0L), Some(true)).head.id.get
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

        if(cloneElements){
          slideElementService.getBySlideId(slide.id.get).foreach { slideElement =>
            slideElementService.clone(
              slideElement.id.get,
              clonedSlide.id.get,
              slideElement.correctLinkedSlideId,
              slideElement.incorrectLinkedSlideId,
              slideElement.width,
              slideElement.height,
              slideElement.content,
              isTemplate,
              slideElement.properties
            )
          }
        }
      }
      clonedSlide
    }
  }

  override def parsePDF(content: Array[Byte], slideId: Long, slideSetId: Long): List[(Long, String)] =
    pdfProcessor.parsePDF(content, slideId, slideSetId)

  override def parsePPTX(content: Array[Byte], slideId: Long, slideSetId: Long, fileName: String): List[(Long, String)] =
    presentationProcessor.parsePPTX(content, slideId, slideSetId, fileName)

  override def addSlidesToSlideSet(slideId: Long, slideSetId: Long, pages: List[BufferedImage]): List[(Long, String)] = {
    var lastSlideId = slideId

    val resultHead = (slideId,
      uploadImage(pages.head,
        getById(slideId)
          .getOrElse(throw new NoSuchElementException("No slide found with id $slideId"))))

    val resultTail = for (p <- pages.tail) yield {
      val currentSlide = create(
        SlideModel(
          slideSetId = slideSetId
        )
      )
      lastSlideId = currentSlide.id.get
      (lastSlideId, uploadImage(p, currentSlide))
    }

    resultHead :: resultTail
  }

  private def uploadImage(image: BufferedImage, slide: SlideModel): String = {
    val ImageFormat = "png"

    val outputStream = new ByteArrayOutputStream
    val fileName = UUID.randomUUID() + "." + ImageFormat
    val folderId = "slide_" + slide.id.get

    try {
      ImageIO.write( image, ImageFormat, outputStream )

      fileService.replaceFileContent(
        folderId,
        fileName,
        outputStream.toByteArray
      )
    }
    finally {
      if (outputStream != null) outputStream.close()
    }

    fileName
  }

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

  implicit def slideModelConversion(entity: SlideEntity, slideElements: List[SlideElementModel]) = {
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