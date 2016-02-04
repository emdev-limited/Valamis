package com.arcusys.valamis.slide.service

import java.io.InputStream

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.service.PackageUploadManager
import com.arcusys.valamis.lesson.tincan.model.PackageCategoryGoal
import com.arcusys.valamis.lesson.tincan.storage.PackageCategoryGoalStorage
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.SlideModelConverters._
import com.arcusys.valamis.slide.service.export._
import com.arcusys.valamis.slide.storage.{SlideThemeRepositoryContract, SlideSetRepository}
import com.arcusys.valamis.uri.model.ValamisURIType
import com.arcusys.valamis.uri.service.URIServiceContract
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class SlideSetService(implicit val bindingModule: BindingModule)
  extends Injectable
  with SlideSetServiceContract {

    private case class SlideCloneMapper(sourceSlideId: Long, clonedSlideId: Long)
    private val slideSetRepository = inject[SlideSetRepository]
    private val slideSetPublisher = inject[SlideSetPublisherContract]
    private val slideSetImporter = inject[SlideSetImporterContract]
    private val slideSetExporter = inject[SlideSetExporterContract]
    private val slideService = inject[SlideServiceContract]
    private val slideElementService = inject[SlideElementServiceContract]
    private val packageUploadService = new PackageUploadManager()
    private val uriService = inject[URIServiceContract]
    private lazy val packageGoalStorage = inject[PackageCategoryGoalStorage]
    private lazy val fileService = inject[FileService]
    private val slideThemeRepository = inject[SlideThemeRepositoryContract]

    override def getById(id: Long) = slideSetRepository.getById(id)

    override def getLogo(slideSetId: Long) = {
      getById(slideSetId)
        .flatMap(s => SlideSetHelper.slideSetLogoPath(s))
        .flatMap(fileService.getFileContentOption)
    }

    override def setLogo(slideSetId: Long, name: String, content: Array[Byte]) = {
      getById(slideSetId).map { slideSet =>
        fileService.setFileContent(
          folder = SlideSetHelper.filePathPrefix(slideSet),
          name = name,
          content = content,
          deleteFolder = true
        )
        update(slideSet.copy(logo = Some(name)))
      }
    }

    override def getSlideSets(titleFilter: String, sortTitleAsc: Boolean, page: Int, itemsOnPage: Int, courseId: Option[Long], isTemplate: Option[Boolean]) =
        slideSetRepository.getSlideSets(titleFilter, sortTitleAsc, page, itemsOnPage, courseId, isTemplate)
          .map(toSlideSetShortModel)


    override def getSlideSetsCount(titleFilter: String, courseId: Option[Long], isTemplate: Option[Boolean]) =
      slideSetRepository.getSlideSetsCount(titleFilter, courseId, isTemplate)

    override def delete(id: Long) = {
      getById(id)
        .flatMap(entity => Some(SlideSetHelper.filePathPrefix(entity)))
        .foreach(fileService.deleteByPrefix)

        slideSetRepository.delete(id)
    }

    override def clone(id: Long, isTemplate: Boolean, fromTemplate: Boolean, title: String, description: String, logo: Option[String]): SlideSetModel = {
      val slideSet = getById(id).getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$id"))
      val clonedSlideSet = create(
        SlideSetModel(
          courseId = slideSet.courseId,
          isTemplate = isTemplate
        )
      )
      val rootSlide = slideService
        .getBySlideSetId(id, Some(fromTemplate))
        .filter(slide => slide.leftSlideId.isEmpty && slide.topSlideId.isEmpty)
      rootSlide.foreach { slide =>
        val slideCloneMapper =
          cloneSlides(
            slide,
            slide.leftSlideId,
            slide.topSlideId,
            id,
            clonedSlideSet.id.get,
            isTemplate,
            fromTemplate,
            slide.isLessonSummary)

        slideCloneMapper.foreach { clonedSlides =>
          slideSet.slides
            .foreach { slide =>
              updateSlideRefs(slide, clonedSlides)
          }
        }
      }

      slideSet.logo.foreach { logo =>
        fileService.copyFile(
          SlideSetHelper.filePathPrefix(slideSet),
          logo,
          SlideSetHelper.filePathPrefix(clonedSlideSet),
          logo,
          false
        )
      }
      update(
        SlideSetModel(
          clonedSlideSet.id,
          if(fromTemplate) title else slideSet.title,
          if(fromTemplate) description else slideSet.description,
          slideSet.courseId,
          if(fromTemplate) logo else slideSet.logo,
          isTemplate = isTemplate
        )
      )
    }

    private def cloneSlides(sourceSlideModel: SlideModel,
                    leftSlideId: Option[Long],
                    topSlideId: Option[Long],
                    sourceSlideSetId: Long,
                    clonedSlideSetId: Long,
                    isTemplate: Boolean,
                    fromTemplate: Boolean,
                    isLessonSummary: Boolean): Option[List[SlideCloneMapper]] = {
      var slideCloneMapper: List[SlideCloneMapper] = Nil
      val clonedSlide =
        slideService.clone(
          sourceSlideModel.id.get,
          leftSlideId,
          topSlideId,
          sourceSlideModel.bgImage,
          clonedSlideSetId,
          isTemplate,
          isLessonSummary,
          fromTemplate,
          true)

      clonedSlide.map { cloned =>
        //TODO get slideService.rightSlides && get slideService.bottomSlides
        val rightSlides = slideService
          .getBySlideSetId(sourceSlideSetId, Some(fromTemplate))
          .filter(slide => slide.leftSlideId == sourceSlideModel.id)
        val bottomSlides = slideService
          .getBySlideSetId(sourceSlideSetId, Some(fromTemplate))
          .filter(slide => slide.topSlideId == sourceSlideModel.id)

        rightSlides.foreach(sourceSlide =>
          slideCloneMapper =
            cloneSlides(
              sourceSlide,
              cloned.id,
              None,
              sourceSlideSetId,
              clonedSlideSetId,
              isTemplate,
              fromTemplate,
              isLessonSummary
            ).getOrElse(Nil) ++ slideCloneMapper
        )
        bottomSlides.foreach(sourceSlide =>
          slideCloneMapper =
            cloneSlides(
              sourceSlide,
              None,
              cloned.id,
              sourceSlideSetId,
              clonedSlideSetId,
              isTemplate,
              fromTemplate,
              isLessonSummary
            ).getOrElse(Nil) ++ slideCloneMapper
        )
        SlideCloneMapper(sourceSlideModel.id.get, cloned.id.get) :: slideCloneMapper
      }
    }

    private def getClonedSlideId(slideId: Option[Long], slideCloneMapper: List[SlideCloneMapper]): Option[Long] = {
      slideId flatMap ( id => slideCloneMapper.find(_.sourceSlideId == id).map(_.clonedSlideId))
    }

    private def updateSlideRefs(slide: SlideModel, slideCloneMapper: List[SlideCloneMapper]) = {
      getClonedSlideId(slide.id, slideCloneMapper) foreach { clonedSlideId =>
        slideElementService.getBySlideId(slide.id.get) foreach { slideElement =>
          val clonedSlideElement = slideElementService.getBySlideId(clonedSlideId).head
          val newCorrectLinkedSlideId = getClonedSlideId(slideElement.correctLinkedSlideId, slideCloneMapper)
          val newIncorrectLinkedSlideId = getClonedSlideId(slideElement.incorrectLinkedSlideId, slideCloneMapper)

          slideElementService.update(
            SlideElementModel(
              clonedSlideElement.id,
              clonedSlideElement.top,
              clonedSlideElement.left,
              clonedSlideElement.width,
              clonedSlideElement.height,
              clonedSlideElement.zIndex,
              clonedSlideElement.content,
              clonedSlideElement.slideEntityType,
              clonedSlideId,
              newCorrectLinkedSlideId,
              newIncorrectLinkedSlideId,
              clonedSlideElement.notifyCorrectAnswer,
              clonedSlideElement.properties
            )
          )
        }
      }
    }

    override def publishSlideSet(id: Long, userId: Long, learnPortletPath: String, courseId: Long) : Unit = {
      val slideSet = getById(id).getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$id"))

      val packageFile = slideSetPublisher.composeTinCanPackage(id, learnPortletPath, slideSet.title, slideSet.description)

      val categories = for (
        slide <- slideService.getBySlideSetId(id, Some(false));
        statementCategoryId <- slide.statementCategoryId;
        uri <-  uriService.getById(statementCategoryId, ValamisURIType.Category)
      ) yield uri

      val (packageId, _) = packageUploadService.uploadPackage(
        slideSet.title,
        slideSet.description,
        slideSet.logo.map { (s"slideset_logo_${slideSet.id.get}", _) },
        courseId,
        userId,
        packageFile
      )

      val packageGoals = categories
        .groupBy(identity).toSeq
        .map(c => PackageCategoryGoal(
          packageId = packageId,
          name = c._1.content,
          category = c._1.uri,
          count = c._2.size
        ))

      packageGoalStorage.add(packageGoals)

      packageFile.delete()
    }

    override def exportSlideSet(id: Long) = {
      val slideSet = getById(id).getOrElse(throw new IllegalStateException(s"No slideSet exist with id $id"))

      slideSetExporter.exportItems(Seq(slideSet))
    }

    override def importSlideSet(stream: InputStream, scopeId: Int) =
      slideSetImporter.importItems(stream, scopeId)

    override def update(slideSetModel: SlideSetModel) = {
      val hasTheme = slideSetModel.themeId.map(slideThemeRepository.isExist).getOrElse(true)
      val slideSet = if (!hasTheme) slideSetModel.copy(themeId = None) else slideSetModel
      slideSetRepository.update(slideSet)
    }


    override def create(slideSetModel: SlideSetModel) =
      slideSetRepository.create(slideSetModel)

    override def createWithDefaultSlide(slideSetModel: SlideSetModel) = {
      val slideSet = slideSetRepository.create(slideSetModel)
      slideService.create(
        SlideModel(
          title = "Page 1",
          slideSetId = slideSet.id.get
        )
      )

      slideSet
    }

  private def toSlideSetShortModel(from: SlideSetEntity): SlideSetModel ={
    val slidesCount = slideService.getBySlideSetId(from.id.get, None).length
    slideSetModelConversion(from, Nil, Some(slidesCount))
  }

  private implicit def convertToModel(from: SlideSetEntity): SlideSetModel =
    slideSetModelConversion(from, slideService.getBySlideSetId(from.id.get, None))

  private implicit def convertToModelList(from: List[SlideSetEntity]): List[SlideSetModel] = from.map(convertToModel)
  private implicit def convertToModelOption(from: Option[SlideSetEntity]):Option[SlideSetModel] = from.map(convertToModel)

}