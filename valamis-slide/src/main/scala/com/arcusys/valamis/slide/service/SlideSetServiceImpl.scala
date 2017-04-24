package com.arcusys.valamis.slide.service

import java.io.{FileInputStream, InputStream}
import java.util.UUID

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.model.{Order, RangeResult, SkipTake}
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.export.SlideSetHelper._
import com.arcusys.valamis.slide.service.export._
import com.arcusys.valamis.slide.storage.{SlideSetRepository, SlideThemeRepository}
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.util.Joda.dateTimeOrdering
import org.joda.time.DateTime

import scala.util.Try

abstract class SlideSetServiceImpl extends SlideSetService {

  def slideSetRepository: SlideSetRepository
  def slideSetImporter: SlideSetImporter
  def slideSetExporter: SlideSetExporter
  def slideService: SlideService
  def slideElementService: SlideElementService
  def uriService: TincanURIService
  def fileService: FileService
  def slideThemeRepository: SlideThemeRepository
  def courseService: CourseService
  def slideTagService: TagService[SlideSet]
  def slideAssetHelper: SlideSetAssetHelper

  override def getById(id: Long): SlideSet =
    slideSetRepository.getById(id)
      .getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$id"))

  override def getLogo(id: Long): Option[Array[Byte]] = {
    SlideSetHelper.slideSetLogoPath(getById(id))
      .flatMap(fileService.getFileContentOption)
  }

  override def setLogo(id: Long, name: String, content: Array[Byte]): Unit = {
    fileService.setFileContent(
      folder = SlideSetHelper.filePathPrefix(getById(id)),
      name = name,
      content = content,
      deleteFolder = true
    )
    slideSetRepository.updateLogo(id, Some(name))
  }

  override def deleteLogo(id: Long): Unit = {
    fileService.deleteByPrefix(filePathPrefix(getById(id)))
    slideSetRepository.updateLogo(id, None)
  }

  override def getSlideSets(courseId: Long,
                            titleFilter: Option[String],
                            sortBy: SlideSetSort,
                            skipTake: Option[SkipTake],
                            isTemplate: Boolean): RangeResult[SlideSet] = {

    if (isTemplate) {
      getTemplates
    }
    else {
      getSlideSets(courseId, titleFilter, sortBy, skipTake)
    }
  }

  private def getSlideSets(courseId: Long,
                           titleFilter: Option[String],
                           sortBy: SlideSetSort,
                           skipTake: Option[SkipTake]): RangeResult[SlideSet] = {

    val sets = slideSetRepository.getByCourseId(courseId, titleFilter)

    val activityToVersion = sets
      .groupBy(_.activityId)
      .map { case (activityId, x) =>
        activityId -> x.map(_.version).max
      }

    val result = for {
      s <- sets
      a <- activityToVersion
      if s.activityId == a._1 && s.version == a._2
    } yield s

    val orderedResult = sortBy match {
      case SlideSetSort(SlideSetSortBy.ModifiedDate, Order.Asc) => result.sortBy(_.modifiedDate)
      case SlideSetSort(SlideSetSortBy.ModifiedDate, Order.Desc) => result.sortBy(_.modifiedDate).reverse
      case SlideSetSort(SlideSetSortBy.Name, Order.Asc) => result.sortBy(_.title)
      case SlideSetSort(SlideSetSortBy.Name, Order.Desc) => result.sortBy(_.title.reverse)
      case _ => throw new NotImplementedError("SlideSet sort")
    }

    val slicedResult = skipTake match {
      case Some(SkipTake(skip, take)) => orderedResult.slice(skip, skip + take)
      case _ => orderedResult
    }

    RangeResult(result.size, slicedResult)
  }

  private def getTemplates: RangeResult[SlideSet] = {
    val sets = slideSetRepository.getTemplates
    RangeResult(sets.size, sets)
  }

  private def getTags(tags: Seq[(Long, Seq[ValamisTag])], slideSetId: Long): Seq[ValamisTag] = {
    tags.filter(_._1 == slideSetId)
      .map(_._2)
      .headOption
      .getOrElse(Nil)
  }

  override def delete(id: Long): Unit = {
    Some(SlideSetHelper.filePathPrefix(getById(id)))
      .foreach(fileService.deleteByPrefix)

    slideAssetHelper.deleteSlideAsset(id)
    slideSetRepository.delete(id)
  }

  override def clone(id: Long,
                     isTemplate: Boolean,
                     fromTemplate: Boolean,
                     title: String,
                     description: String,
                     logo: Option[String],
                     newVersion: Option[Boolean] = None): SlideSet = {

    val slideSet = getById(id)

    if (newVersion.isEmpty || (newVersion.isDefined && slideSet.status != SlideSetStatus.Draft)) {
      val titlePrefix = "copy"

      val slideSetTitle =
        if (newVersion.isEmpty)
          getTitle(slideSet.title, slideSet, titlePrefix)
        else
          slideSet.title

      val activityId = if (newVersion.isDefined) slideSet.activityId else createNewActivityId(slideSet.courseId)
      val status = if (newVersion.isDefined) slideSet.status else "draft"
      val version = if (newVersion.isDefined) slideSet.version else 1.0

      val tags = slideAssetHelper.getSlideAssetCategories(slideSet.id).map(_.id.toString)

      val hasTheme = slideSet.themeId.map(slideThemeRepository.isExist).getOrElse(true)

      val copiedSlideSet = if (fromTemplate)
        slideSet.copy(title = title, description = description, logo = logo)
      else
        slideSet.copy(title = slideSetTitle)

      val clonedSlideSet = create(
        copiedSlideSet.copy(
          isTemplate = isTemplate,
          activityId = activityId,
          status = status,
          version = version,
          themeId = if (!hasTheme) None else slideSet.themeId,
          lockDate = None,
          lockUserId = None)
      )

      updateTags(clonedSlideSet, tags)

      val slidesMapper = scala.collection.mutable.Map[Long, Long]()

      val slides = slideService.getSlides(id)

      val rootSlide = slideService.getRootSlide(slides, fromTemplate)

      rootSlide.foreach { slide =>
        cloneSlides(
          slide,
          slide.leftSlideId,
          slide.topSlideId,
          id,
          clonedSlideSet.id,
          isTemplate,
          fromTemplate,
          slides,
          slidesMapper)

        slidesMapper.foreach { case (oldSlideId, newSlideId) =>
          for {
            slide <- slides
            slideElement <- slide.slideElements.filter(_.slideId == oldSlideId)
          } {
            val correctLinkedSlideId = slideElement.correctLinkedSlideId.flatMap(oldId => slidesMapper.get(oldId))
            val incorrectLinkedSlideId = slideElement.incorrectLinkedSlideId.flatMap(oldId => slidesMapper.get(oldId))
            slideElementService.clone(
              slideElement.copy(
                correctLinkedSlideId = correctLinkedSlideId,
                incorrectLinkedSlideId = incorrectLinkedSlideId,
                slideId = newSlideId),
              isTemplate)
          }
        }
      }

      slideSet.logo.foreach { logo =>
        fileService.copyFile(
          SlideSetHelper.filePathPrefix(slideSet),
          logo,
          SlideSetHelper.filePathPrefix(clonedSlideSet),
          logo,
          deleteFolder = false
        )
      }
      clonedSlideSet
    }
    else slideSet
  }

  private def getTitle(title: String, slideSet: SlideSet, titlePrefix: String) = {
    val cleanedTitle = cleanTitle(title, titlePrefix)
    val slideSets = slideSetRepository
      .getByCourseId(slideSet.courseId, Some(cleanedTitle + s" $titlePrefix"))
      .sortBy(_.title) ++ Seq(slideSet)
    val maxIndex = slideSets.map(s => copyIndex(s.title, titlePrefix)).max
    cleanedTitle + s" $titlePrefix " + (maxIndex + 1)
  }

  private def copyIndex(title: String, titlePrefix: String): Int = {
    val copyRegex = (" " + titlePrefix + " (\\d+)$").r
    copyRegex.findFirstMatchIn(title)
      .flatMap(str => Try(str.group(1).toInt).toOption)
      .getOrElse(0)
  }

  private def cleanTitle(title: String, titlePrefix: String): String = {
    val cleanerRegex = ("(.*) " + titlePrefix + " \\d+$").r
    title match {
      case cleanerRegex(text) => text.trim
      case _ => title
    }
  }

  private def cloneSlides(sourceSlideModel: Slide,
                          leftSlideId: Option[Long],
                          topSlideId: Option[Long],
                          sourceSlideSetId: Long,
                          clonedSlideSetId: Long,
                          isTemplate: Boolean,
                          fromTemplate: Boolean,
                          slides: Seq[Slide],
                          slidesMapper: scala.collection.mutable.Map[Long, Long]): Unit = {

    val clonedSlide =
      slideService.clone(
        sourceSlideModel,
        leftSlideId,
        topSlideId,
        clonedSlideSetId,
        isTemplate,
        fromTemplate)

    val rightSlides = slideService.getRightSlide(slides, sourceSlideModel.id, fromTemplate)
    val bottomSlides = slideService.getBottomSlide(slides, sourceSlideModel.id, fromTemplate)

    rightSlides.foreach(sourceSlide =>
      cloneSlides(
        sourceSlide,
        Some(clonedSlide.id),
        None,
        sourceSlideSetId,
        clonedSlideSetId,
        isTemplate,
        fromTemplate,
        slides,
        slidesMapper))
    bottomSlides.foreach(sourceSlide =>
      cloneSlides(
        sourceSlide,
        None,
        Some(clonedSlide.id),
        sourceSlideSetId,
        clonedSlideSetId,
        isTemplate,
        fromTemplate,
        slides,
        slidesMapper))

    slidesMapper += (sourceSlideModel.id -> clonedSlide.id)
  }

  override def exportSlideSet(id: Long): FileInputStream = {
    val slideSet = getById(id)
    slideSetExporter.exportItems(Seq(slideSet))
  }

  override def importSlideSet(stream: InputStream, scopeId: Int): Unit =
    slideSetImporter.importItems(stream, scopeId)

  def updateInfo(id: Long, title: String, description: String, tags: Seq[String]): Unit = {
    slideSetRepository.getById(id) foreach { s =>
      updateTags(s, tags)
      slideSetRepository.update(id, title, description)
    }
  }

  override def updateSettings(id: Long,
                              isSelectedContinuity: Boolean,
                              themeId: Option[Long],
                              duration: Option[Long],
                              scoreLimit: Option[Double],
                              playerTitle: String,
                              topDownNavigation: Boolean,
                              oneAnswerAttempt: Boolean,
                              requiredReview: Boolean): Unit = {

    slideSetRepository.getById(id) foreach { s =>
      val list = slideSetRepository.getByActivityId(s.activityId)
      val isPublished = s.status == SlideSetStatus.Published || s.status == SlideSetStatus.Archived
      if (isPublished && list.exists(_.status == SlideSetStatus.Draft)) {
        list.filter(_.status == SlideSetStatus.Draft).foreach(x => delete(x.id))
      }
      val maxVersion = slideSetRepository.getByActivityId(s.activityId).map(_.version).max
      val (status, version)  = if (isPublished) {
        (SlideSetStatus.Draft, (math floor (maxVersion + 0.1) * 100) / 100)
      }
      else {
        (SlideSetStatus.Draft, s.version)
      }
      val hasTheme = themeId.map(slideThemeRepository.isExist).getOrElse(true)
      val newThemeId = if (!hasTheme) None else themeId

      slideSetRepository.update(id,
        isSelectedContinuity,
        newThemeId,
        duration,
        scoreLimit,
        playerTitle,
        topDownNavigation,
        status,
        version,
        oneAnswerAttempt,
        requiredReview)
    }
  }

  override def create(slideSetModel: SlideSet, tags: Seq[String] = Seq()): SlideSet = {
    val slideSet = slideSetRepository.create(slideSetModel)
    updateTags(slideSet, tags)
    slideSet
  }

  override def createWithDefaultSlide(slideSetModel: SlideSet, tags: Seq[String] = Seq()): SlideSet = {
    val slideSet = slideSetRepository.create(slideSetModel.copy(activityId = createNewActivityId(slideSetModel.courseId)))
    updateTags(slideSet, tags)
    slideService.create(
      Slide(
        title = "Page 1",
        slideSetId = slideSet.id
      )
    )
    slideSet
  }

  override def getVersions(id: Long): Seq[SlideSet] = {
    val slideSet = getById(id)
    val slides = slideSetRepository.getByVersion(slideSet.activityId, slideSet.version)
    slides
  }

  override def deleteAllVersions(id: Long): Unit = {
    val slideSet = getById(id)
    if (slideSet.activityId.nonEmpty) {
      slideSetRepository.getByActivityId(slideSet.activityId).foreach { slideSet =>
        slideSetRepository.delete(slideSet.id)
        slideAssetHelper.deleteSlideAsset(slideSet.id)
      }
    }
    else {
      slideSetRepository.delete(slideSet.id)
    }
  }

  override def createNewActivityId(courseId: Long): String = {
    val uriType = TincanURIType.Course
    val id = UUID.randomUUID.toString
    val companyId = courseService.getById(courseId).map(g => g.getCompanyId)
    val prefix = uriService.getLocalURL(companyId = companyId)
    s"$prefix$uriType/${uriType}_$id"
  }

  private def updateTags(slideSet: SlideSet, tags: Seq[String]): Unit = {
    val tagIds = slideTagService.getOrCreateTagIds(tags, CompanyHelper.getCompanyId)
    val assetId = slideAssetHelper.updateSlideAsset(slideSet, None)
    slideTagService.setTags(assetId, tagIds)
  }


  def changeLockStatus(slideSetId: Long, userId: Option[Long]) = {
    val lockDate = userId match {
      case Some(id) => Some(DateTime.now)
      case _ => None
    }
    slideSetRepository.updateLockUser(slideSetId, userId, lockDate)
  }
}