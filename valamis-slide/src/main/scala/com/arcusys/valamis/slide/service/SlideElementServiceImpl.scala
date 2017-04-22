package com.arcusys.valamis.slide.service

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.slide.storage.{SlideElementPropertyRepository, SlideElementRepository}
import com.arcusys.valamis.slide.model.SlideEntityType._

abstract class SlideElementServiceImpl extends SlideElementService {

  def slideElementRepository: SlideElementRepository
  def slideElementPropertyRepository: SlideElementPropertyRepository
  def fileService: FileService

  override def getById(id: Long): Option[SlideElement] = slideElementRepository.getById(id)

  override def getBySlideId(slideId: Long): Seq[SlideElement] = slideElementRepository.getBySlideId(slideId)

  override def getLogo(id: Long): Option[Array[Byte]] = {
    def getLogo(slideElement: SlideElement) = {
      fileService.getFileContentOption(
        logoPath(slideElement, slideElement.content)
      ).get
    }
    getById(id)
      .map(getLogo)
  }

  override def setLogo(id: Long, name: String, content: Array[Byte]): Unit = {
    getById(id).map { slideElement =>
      fileService.setFileContent(
        folder = filePathPrefix(slideElement),
        name = name,
        content = content,
        deleteFolder = false
      )
      update(slideElement.copy(
        content = name
      )
      )
    }
  }

  override def create(element: SlideElement): SlideElement = {
    slideElementRepository.create(element)
  }

  override def update(element: SlideElement): SlideElement = {
    slideElementRepository.update(element)
  }

  override def updateContent(id: Long, content: String): Unit = {
    slideElementRepository.updateContent(id, content)
  }

  override def delete(id: Long): Unit = {
    val slideElement = getById(id)
    slideElement
      .flatMap(slideItemPath)
      .foreach(fileService.deleteFile)

    slideElementRepository.delete(id)
  }

  override def clone(element: SlideElement, isTemplate: Boolean): SlideElement = {

    val cloneContent =
      if (isTemplate) {
        if (element.slideEntityType == "text") "New text element"
        else ""
      }
      else
        element.content

    val clonedSlideElement = create(element.copy(content = cloneContent))
    if (!isTemplate && !element.content.isEmpty && !element.content.contains("/")
      && SlideEntityType.AvailableExternalFileTypes.contains(element.slideEntityType)) {

      val newPath = element.slideEntityType match {
        case Image | Webgl | Audio =>
          val fileName = element.content.takeWhile(_ != ' ')
          try {
            fileService.copyFile(
              filePathPrefix(element),
              fileName,
              filePathPrefix(clonedSlideElement),
              fileName,
              deleteFolder = false
            )
            fileName
          }
          catch {
            case e: NoSuchElementException => ""
          }
        case Pdf =>
          val fileName = element.content.reverse.takeWhile(_ != '/').reverse
          try {
            fileService.copyFile(
              "slideData" + element.id,
              fileName,
              "slideData" + clonedSlideElement.id,
              fileName,
              deleteFolder = false
            )
            element.content.replace(s"slideData${element.id}", "slideData" + clonedSlideElement.id)
          }
          catch {
            case e: NoSuchElementException => ""
          }
      }
      updateContent(clonedSlideElement.id, newPath)
    }
    clonedSlideElement
  }

  private def slideItemPath(element: SlideElement): Option[String] = {
    element.slideEntityType match {
      case Image | Webgl | Audio =>
        if (element.content != "") {
          Some(s"slide_item_${element.id}/${element.content}")
        }
        else {
          None
        }
      case Pdf =>
        if (element.content != "") {
          Some(s"slideData${element.id}/${element.content}")
        }
        else {
          None
        }
      case _ => None
    }
  }
}
