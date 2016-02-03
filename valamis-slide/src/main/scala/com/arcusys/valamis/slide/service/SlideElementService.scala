package com.arcusys.valamis.slide.service

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.slide.exeptions.NoSlideElementException
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
      case Text | Iframe | Question | Video | Math | PlainText => None
    }
  }

  override def create(slideElement: SlideElementModel) = {
    val newSlideElement = slideElementRepository.create(slideElement)
    slideElement.properties.foreach(createProperties(newSlideElement.id.get, _))
    newSlideElement
  }

  private def createProperties(slideElementId: Long, elementProperties: SlideElementsProperties) = {
    val deviceId = elementProperties.deviceId
    elementProperties.properties.foreach(property =>
      slideElementPropertyRepository.create(
        SlideElementPropertyEntity(
          slideElementId,
          deviceId,
          property.key,
          property.value)
      )
    )
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

  override def update(slideElement: SlideElementModel) = {
    val updatedElement = slideElementRepository.update(slideElement)
    slideElementPropertyRepository.delete(updatedElement.id.get)
    slideElement.properties.foreach(createProperties(updatedElement.id.get, _))
    updatedElement
  }

  override def delete(id: Long) = {
    val slideElement = slideElementRepository.getById(id)
    slideElement
      .flatMap(slideItemPath)
      .foreach(fileService.deleteFile)

    slideElementRepository.delete(id)
  }

  //TODO: too many arguments for clone command, check and remove unnecessary
  override def clone(id: Long,
                     slideId: Long,
                     correctLinkedSlideId: Option[Long],
                     incorrectLinkedSlideId: Option[Long],
                     width: String,
                     height: String,
                     content: String,
                     isTemplate: Boolean,
                     properties: Seq[SlideElementsProperties]): SlideElementModel = {
    import com.arcusys.valamis.slide.model.SlideEntityType._

    val slideElement = getById(id).getOrElse(
      throw new NoSlideElementException(id)
    )

    val cloneContent =
      if (isTemplate) {
        if (slideElement.slideEntityType == "text") "New text element"
        else ""
      }
      else
        content

    val clonedSlideElement = create(
      SlideElementModel(None,
        slideElement.top,
        slideElement.left,
        width,
        height,
        slideElement.zIndex,
        cloneContent,
        slideElement.slideEntityType,
        slideId,
        correctLinkedSlideId,
        incorrectLinkedSlideId,
        slideElement.notifyCorrectAnswer,
        properties
      )
    )
    if (!isTemplate
      && !slideElement.content.isEmpty
      && (!slideElement.content.contains("/") /* || slideElement.content.contains("/pdf/web/viewer.html")*/)
      && SlideEntityType.AvailableExternalFileTypes.contains(slideElement.slideEntityType)) {

      val newPath = slideElement.slideEntityType match {
        case Image | Webgl =>
          val fileName = slideElement.content.takeWhile(_ != ' ')
          fileService.copyFile(
            filePathPrefix(slideElement),
            fileName,
            filePathPrefix(clonedSlideElement),
            fileName,
            false
          )
          fileName
        case Pdf =>
          val fileName = slideElement.content.reverse.takeWhile(_ != '/').reverse
          fileService.copyFile(
            "slideData" + slideElement.id.get,
            fileName,
            "slideData" + clonedSlideElement.id.get,
            fileName,
            false
          )
          slideElement.content.replace(s"slideData${slideElement.id.get}", "slideData" + clonedSlideElement.id.get)
      }
      update(
        SlideElementModel(
          clonedSlideElement.id,
          slideElement.top,
          slideElement.left,
          width,
          height,
          slideElement.zIndex,
          newPath,
          slideElement.slideEntityType,
          slideId,
          correctLinkedSlideId,
          incorrectLinkedSlideId,
          slideElement.notifyCorrectAnswer,
          properties
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
      entity.top,
      entity.left,
      entity.width,
      entity.height,
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
