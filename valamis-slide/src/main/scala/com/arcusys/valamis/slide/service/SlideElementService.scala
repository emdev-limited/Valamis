package com.arcusys.valamis.slide.service

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.SlideModelConverters._
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.slide.storage.{SlideElementPropertyRepository, SlideElementRepository}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class SlideElementService(implicit val bindingModule: BindingModule)
  extends Injectable with SlideElementServiceContract {

  private val slideElementRepository = inject[SlideElementRepository]
  private val slideElementPropertyRepository = inject[SlideElementPropertyRepository]
  private val fileService = inject[FileService]

  private def slideItemPath(slideElement: SlideElementEntity): Option[String] = {
    import com.arcusys.valamis.slide.model.SlideEntityType._

    slideElement.slideEntityType match {
      case Image | Webgl =>
        if (slideElement.content != "") {
          Some(s"slide_item_${slideElement.id}/${slideElement.content}")
        }
        else None
      case Pdf =>
        if (slideElement.content != "") {
          Some(s"slideData${slideElement.id}/${slideElement.content}")
        }
        else None
      case _ => None
    }
  }

  override def create(slideElement: SlideElementModel): SlideElementModel = {
    val newSlideElement = slideElementRepository.create(slideElement)
    slideElementPropertyRepository.create(slideElement, newSlideElement.id.get)
    newSlideElement
  }

  override def getAll = slideElementRepository.getAll

  override def getById(id: Long) = slideElementRepository.getById(id: Long)

  override def getBySlideId(slideId: Long) = slideElementRepository.getBySlideId(slideId)

  override def getLogo(slideElementId: Long) = {
    def getLogo(slideElement: SlideElementModel) = {
      fileService.getFileContentOption(
        logoPath(slideElement, slideElement.content)
      ).get
    }
    getById(slideElementId)
      .map(getLogo)
  }

  override def setLogo(slideElementId: Long, name: String, content: Array[Byte]) = {
    getById(slideElementId).map { slideElement =>
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

  override def update(slideElement: SlideElementModel): SlideElementModel = {
    val updatedElement = slideElementRepository.update(slideElement)
    slideElementPropertyRepository.replace(slideElement, updatedElement.id.get)
    updatedElement
  }

  override def delete(id: Long) = {
    val slideElement = slideElementRepository.getById(id)
    slideElement
      .flatMap(slideItemPath)
      .foreach(fileService.deleteFile)

    slideElementRepository.delete(id)
  }

  override def clone(slideElement: SlideElementModel,
                     slideId: Long,
                     isTemplate: Boolean): SlideElementModel = {
    import com.arcusys.valamis.slide.model.SlideEntityType._

    val cloneContent =
      if (isTemplate) {
        if (slideElement.slideEntityType == "text") "New text element"
        else ""
      }
      else
        slideElement.content

    val clonedSlideElement = create(
      SlideElementModel(
        None,
        slideElement.zIndex,
        cloneContent,
        slideElement.slideEntityType,
        slideId,
        slideElement.correctLinkedSlideId,
        slideElement.incorrectLinkedSlideId,
        slideElement.notifyCorrectAnswer,
        slideElement.properties
      )
    )
    if (!isTemplate
      && !slideElement.content.isEmpty
      && (!slideElement.content.contains("/") /* || slideElement.content.contains("/pdf/web/viewer.html")*/)
      && SlideEntityType.AvailableExternalFileTypes.contains(slideElement.slideEntityType)) {

      val newPath = slideElement.slideEntityType match {
        case Image | Webgl =>
          val fileName = slideElement.content.takeWhile(_ != ' ')
          try {
            fileService.copyFile(
              filePathPrefix(slideElement),
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
          val fileName = slideElement.content.reverse.takeWhile(_ != '/').reverse
          try {
            fileService.copyFile(
              "slideData" + slideElement.id.get,
              fileName,
              "slideData" + clonedSlideElement.id.get,
              fileName,
              deleteFolder = false
            )
            slideElement.content.replace(s"slideData${slideElement.id.get}", "slideData" + clonedSlideElement.id.get)
          }
          catch {
            case e: NoSuchElementException => ""
          }
      }
      update(
        SlideElementModel(
          clonedSlideElement.id,
          slideElement.zIndex,
          newPath,
          slideElement.slideEntityType,
          slideId,
          slideElement.correctLinkedSlideId,
          slideElement.incorrectLinkedSlideId,
          slideElement.notifyCorrectAnswer,
          slideElement.properties
        )
      )
    }
    clonedSlideElement
  }

  def getProperties(slideElementId: Long): Seq[SlideElementsProperties] = {
    val propertyList = slideElementPropertyRepository.getBySlideElementId(slideElementId)

    val grouped = propertyList.groupBy(_.deviceId)

    val mapped = grouped.map(group =>
      SlideElementsProperties(
        group._1,
        group._2
      )
    )
    mapped.toSeq
  }

  implicit def slideElementModelConversion(entity: SlideElementEntity): SlideElementModel = {
    val properties = getProperties(entity.id.get)

    SlideElementModel(
      entity.id,
      entity.zIndex,
      entity.content,
      entity.slideEntityType,
      entity.slideId,
      entity.correctLinkedSlideId,
      entity.incorrectLinkedSlideId,
      entity.notifyCorrectAnswer,
      properties)
  }

  implicit def slideElementListModelConversion(list: List[SlideElementEntity]): List[SlideElementModel] = list.map(slideElementModelConversion)

  implicit def slideElementOptionModelConversion(list: Option[SlideElementEntity]): Option[SlideElementModel] = list.map(slideElementModelConversion)

}
