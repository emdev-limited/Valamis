package com.arcusys.valamis.slide.service

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.slide.convert.{PDFProcessor, PresentationProcessor}
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.slide.storage.{SlideRepository, SlideSetRepository}

abstract class SlideServiceImpl extends SlideService {

  def slideRepository: SlideRepository
  def slideSetRepository: SlideSetRepository
  def fileService: FileService
  def presentationProcessor: PresentationProcessor
  def pdfProcessor: PDFProcessor
  def slideThemeService: SlideThemeService

  private lazy val lineBreakStr = "[\u2028\u2029]+"

  override def getById(id: Long): Option[Slide] = slideRepository.getById(id)

  override def getRootSlide(slides: Seq[Slide], isTemplate: Boolean = false): Option[Slide] =
    slides.find(s => s.leftSlideId.isEmpty && s.topSlideId.isEmpty && s.isTemplate == isTemplate)

  override def getRightSlide(slides: Seq[Slide], linkId: Long, isTemplate: Boolean = false): Option[Slide] =
    slides.find(s => s.leftSlideId.contains(linkId) && s.isTemplate == isTemplate)

  override def getBottomSlide(slides: Seq[Slide],linkId: Long, isTemplate: Boolean = false): Option[Slide] =
    slides.find(s => s.topSlideId.contains(linkId) && s.isTemplate == isTemplate)

  override def getSlides(slideSetId: Long): Seq[Slide] = {
    getSlideData(slideSetId)
  }

  override def getCount(slideSetId: Long): Long = {
    slideRepository.getCountBySlideSetId(slideSetId)
  }

  override def getTemplateSlides: Seq[Slide] = {
    val templatesIds = slideSetRepository.getTemplates.map(_.id)
    templatesIds flatMap (getSlideData(_, isTemplate = true))
  }

  override def getBgImage(id: Long): Option[Array[Byte]] = {
    def getLogo(slide: Slide) = {
      slide.bgImage
        .map(bgImage => logoPath(slide, bgImage))
        .flatMap(fileService.getFileContentOption)
        .get
    }

    getById(id)
      .map(getLogo)
  }

  override def setBgImage(id: Long, name: String, bgSize: String, content: Array[Byte]): Unit = {
    getById(id).foreach { slide =>
      fileService.setFileContent(
        folder = filePathPrefix(slide),
        name = name,
        content = content,
        deleteFolder = false
      )
      updateBgImage(id, Some(name + ' ' + bgSize))
    }
  }

  override def deleteBgImage(id: Long): Unit = {
    getById(id).foreach { slide =>
      fileService.deleteByPrefix(filePathPrefix(slide))
      updateBgImage(id, None)
    }
  }

  override def delete(id: Long): Unit = {
    getById(id)
      .flatMap(entity => Some(filePathPrefix(entity)))
      .foreach(fileService.deleteByPrefix)

    slideRepository.delete(id)
  }

  override def create(slide: Slide): Slide = {
    slideRepository.create(slide)
  }

  override def update(slide: Slide): Slide = {
    slideRepository.update(slide)
  }

  override def isAvailableLink(id: Long): Boolean = {
    slideRepository.getByLinkSlideId(id).size <= 1
  }

  def updateBgImage(slideId: Long, bgImage: Option[String]): Unit = {
    slideRepository.updateBgImage(slideId, bgImage)
  }

  override def clone(slide: Slide,
                     leftSlideId: Option[Long],
                     topSlideId: Option[Long],
                     slideSetId: Long,
                     isTemplate: Boolean,
                     fromTemplate: Boolean): Slide = {
    val newSlideSetId =
      if(slideSetId > 0) {
        slideSetId
      }
      else {
        val set = slideSetRepository.getTemplates.head
        set.id
      }

    val clonedSlide = create(slide.copy(
      leftSlideId = if(slideSetId > 0) leftSlideId else None,
      topSlideId = if(slideSetId > 0) topSlideId  else None,
      slideSetId = newSlideSetId,
      isTemplate = isTemplate)
    )

      if (!fromTemplate) {
        slide.bgImage.foreach { bgImage =>
          if (!bgImage.isEmpty && !bgImage.contains("/")) {
            copyFile(bgImage, filePathPrefix(slide), filePathPrefix(clonedSlide))
          }
        }
      }
      clonedSlide
  }

  override def parsePDF(content: Array[Byte]):  List[String] =
    pdfProcessor.parsePDF(content)

  override def parsePPTX(content: Array[Byte], fileName: String):  List[String]=
    presentationProcessor.parsePPTX(content, fileName)

  private def copyFile(bgImage: String, sourceName: String, destName: String) = {
    val image = bgImage.takeWhile(_ != ' ')
    fileService.copyFile(
      sourceName,
      image,
      destName,
      image,
      deleteFolder = false
    )
  }

  private def getProperties[T <: PropertyEntity](propertyList: Seq[T]): Seq[Properties] = {
    val grouped = propertyList.groupBy(_.deviceId)
    grouped.map(group =>
      Properties(
        group._1,
        group._2.map(entity =>
          Property(
            entity.key,
            entity.value))
      )
    ).toSeq
  }

  private def getSlideData(slideSetId: Long, isTemplate: Boolean = false): Seq[Slide] = {
    val data = slideRepository.getSlidesWithData(slideSetId, isTemplate)
    data._1 map { s =>
      val properties = data._2.filter(_.slideId == s.id)
      val elements = data._3.filter(_.slideId == s.id)
      val newElements = elements map {e =>
        val eProperties = data._4.filter(_.slideElementId == e.id)
        //in text element's content \u2028 and \u2029 characters(line/paragraph separators) can break JSON
        //we need correct JSON for export and publish slideSet
        val newContent =
          if (e.slideEntityType == SlideEntityType.Text)
            e.content.replaceAll(lineBreakStr, "")
        else e.content
        e.copy(properties = getProperties[SlideElementPropertyEntity](eProperties), content = newContent)
      }
      s.copy(slideElements = newElements, properties = getProperties[SlidePropertyEntity](properties))
    }
  }
}