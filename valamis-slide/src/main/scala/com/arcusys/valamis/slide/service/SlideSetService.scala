package com.arcusys.valamis.slide.service

import java.io.{FileInputStream, InputStream}
import java.util.UUID
import javax.servlet.ServletContext

import com.arcusys.learn.liferay.services.{CompanyHelper, GroupLocalServiceHelper}
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.model.{Lesson, LessonType, PackageActivityType}
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lesson.tincan.model.{LessonCategoryGoal, TincanActivity}
import com.arcusys.valamis.lesson.tincan.service.{LessonCategoryGoalService, TincanPackageService, TincanPackageUploader}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.SlideModelConverters._
import com.arcusys.valamis.slide.service.export._
import com.arcusys.valamis.slide.storage.{SlideSetRepository, SlideThemeRepositoryContract}
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

import scala.util.Try

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
  private val packageUploadService = inject[TincanPackageUploader]
  private val uriService = inject[TincanURIService]
  private lazy val packageCategoryGoalStorage = inject[LessonCategoryGoalService]
  private lazy val fileService = inject[FileService]
  private val slideThemeRepository = inject[SlideThemeRepositoryContract]
  private val courseService = inject[CourseService]
  private val lessonService = inject[LessonService]
  private val tincanPackageService = inject[TincanPackageService]
  private lazy val lessonTagService = inject[TagService[Lesson]]
  private lazy val slideTagService = inject[TagService[SlideSetModel]]
  private lazy val slideAssetHelper = inject[SlideSetAssetHelper]
  private lazy val lessonAssetHelper = inject[LessonAssetHelper]
  private lazy val lessonSocialActivityHelper = new SocialActivityHelper[Lesson]

  override def getById(id: Long): Option[SlideSetModel] = slideSetRepository.getById(id)

  override def getLogo(slideSetId: Long): Option[Array[Byte]] = {
    getById(slideSetId)
      .flatMap(s => SlideSetHelper.slideSetLogoPath(s))
      .flatMap(fileService.getFileContentOption)
  }

  override def setLogo(slideSetId: Long, name: String, content: Array[Byte]): Unit = {
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

  override def getSlideSets(courseId: Long,
                            titleFilter: Option[String],
                            sortTitleAsc: Boolean,
                            skipTake: SkipTake,
                            isTemplate: Option[Boolean]): List[SlideSetModel] = {
    val slides = if (isTemplate.contains(true))
      slideSetRepository.getTemplatesWithCount(courseId)
    else
      slideSetRepository.getLastWithCount(courseId, titleFilter, sortTitleAsc, skipTake)

    val tags = slideAssetHelper.getSlidesAssetCategories(slides.map { case (set, _) => set.id.get })
    slides map { case (set, count) =>
      val slideTags = tags.filter(_._1 == set.id.get)
        .map(_._2)
        .headOption
        .getOrElse(Nil)

      toSlideSetShortModel(set, count, slideTags)
    }
  }

  override def getSlideSetsCount(titleFilter: Option[String], courseId: Long, isTemplate: Option[Boolean]): Int =
    slideSetRepository.getCount(titleFilter, courseId)

  override def delete(id: Long): Unit = {
    getById(id)
      .flatMap(entity => Some(SlideSetHelper.filePathPrefix(entity)))
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
                     newVersion: Option[Boolean] = None) = {

    val slideSet = getById(id)
      .getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$id"))

    if (newVersion.isEmpty || (newVersion.isDefined && slideSet.status != SlideSetStatus.Draft)) {
      val titlePrefix = "copy"

      val activityId = if (newVersion.isDefined) slideSet.activityId else createNewActivityId(slideSet.courseId)
      val status = if (newVersion.isDefined) slideSet.status else "draft"
      val version = if (newVersion.isDefined) slideSet.version else 1.0

      val tags = slideAssetHelper.getSlideAssetCategories(slideSet.id.get).map(_.id.toString)

      val clonedSlideSet = create(
        SlideSetModel(
          courseId = slideSet.courseId,
          isTemplate = isTemplate
        )
      )
      val slidesMapper = scala.collection.mutable.Map[Long, Long]()
      val rootSlide = slideService
        .getBySlideSetId(id, Some(fromTemplate))
        .filter(slide => slide.leftSlideId.isEmpty && slide.topSlideId.isEmpty)
      rootSlide.foreach { slide =>
        cloneSlides(
          slide,
          slide.leftSlideId,
          slide.topSlideId,
          id,
          clonedSlideSet.id.get,
          isTemplate,
          fromTemplate,
          slidesMapper)

        slidesMapper.foreach { case (oldSlideId, newSlideId) =>
          for {
            slide <- slideSet.slides.filter(_.id.contains(oldSlideId))
            slideElement <- slide.slideElements
          } {
            val correctLinkedSlideId = slideElement.correctLinkedSlideId.flatMap(oldId => slidesMapper.get(oldId))
            val incorrectLinkedSlideId = slideElement.incorrectLinkedSlideId.flatMap(oldId => slidesMapper.get(oldId))
            slideElementService.clone(
              slideElement.copy(correctLinkedSlideId = correctLinkedSlideId, incorrectLinkedSlideId = incorrectLinkedSlideId),
              newSlideId,
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
          false
        )
      }

      val slideSetTitle =
        if (newVersion.isEmpty)
          getTitle(slideSet.title, slideSet, titlePrefix)
        else
          slideSet.title

      update(
        SlideSetModel(
          id = clonedSlideSet.id,
          title = if (fromTemplate) title else slideSetTitle,
          description = if (fromTemplate) description else slideSet.description,
          courseId = slideSet.courseId,
          logo = if (fromTemplate) logo else slideSet.logo,
          isTemplate = isTemplate,
          isSelectedContinuity = slideSet.isSelectedContinuity,
          themeId = slideSet.themeId,
          duration = slideSet.duration,
          scoreLimit = slideSet.scoreLimit,
          playerTitle = slideSet.playerTitle,
          topDownNavigation = slideSet.topDownNavigation,
          activityId = activityId,
          status = status,
          version = version,
          oneAnswerAttempt = slideSet.oneAnswerAttempt
        ), tags
      )
    }
  }

  private def getTitle(title: String, slideSet: SlideSetModel, titlePrefix: String) = {
    val cleanedTitle = cleanTitle(title, titlePrefix)
    val slideSets = getSlideSets(
      slideSet.courseId,
      Some(cleanedTitle + s" $titlePrefix"),
      true,
      SkipTake(0, Int.MaxValue),
      Some(false)
    ) ++ Seq(slideSet)
    val maxIndex = slideSets.map(s => copyIndex(s.title, titlePrefix)).max
    cleanedTitle + s" $titlePrefix " + (maxIndex + 1)
  }

  private def copyIndex(title: String, titlePrefix: String): Int = {
    val copyRegex = (" " + titlePrefix + " (\\d+)$").r
    copyRegex.findFirstMatchIn(title)
      .flatMap(str => Try(str.group(1).toInt).toOption)
      .getOrElse(0)
  }

  private def cleanTitle(title: String, titlePrefix: String):String = {
    val cleanerRegex = ("(.*) " + titlePrefix + " \\d+$").r
    title match {
      case cleanerRegex(text) =>text.trim
      case _ => title
    }
  }


  private def cloneSlides(sourceSlideModel: SlideModel,
                          leftSlideId: Option[Long],
                          topSlideId: Option[Long],
                          sourceSlideSetId: Long,
                          clonedSlideSetId: Long,
                          isTemplate: Boolean,
                          fromTemplate: Boolean,
                          slidesMapper: scala.collection.mutable.Map[Long, Long]): Unit = {

    val clonedSlide =
      slideService.clone(
        sourceSlideModel.id.get,
        leftSlideId,
        topSlideId,
        sourceSlideModel.bgImage,
        clonedSlideSetId,
        isTemplate,
        sourceSlideModel.isLessonSummary,
        fromTemplate)

    clonedSlide.foreach { cloned =>
      //TODO get slideService.rightSlides && get slideService.bottomSlides
      val rightSlides = slideService
        .getBySlideSetId(sourceSlideSetId, Some(fromTemplate))
        .filter(slide => slide.leftSlideId == sourceSlideModel.id)
      val bottomSlides = slideService
        .getBySlideSetId(sourceSlideSetId, Some(fromTemplate))
        .filter(slide => slide.topSlideId == sourceSlideModel.id)

      rightSlides.foreach(sourceSlide =>
        cloneSlides(
          sourceSlide,
          cloned.id,
          None,
          sourceSlideSetId,
          clonedSlideSetId,
          isTemplate,
          fromTemplate,
          slidesMapper))
      bottomSlides.foreach(sourceSlide =>
        cloneSlides(
          sourceSlide,
          None,
          cloned.id,
          sourceSlideSetId,
          clonedSlideSetId,
          isTemplate,
          fromTemplate,
          slidesMapper))
    }
    clonedSlide.map(slide => slidesMapper += (sourceSlideModel.id.get -> slide.id.get))
  }

  //TODO: remove method, slide set should not require temp lesson
  override def findSlideLesson(slideSetId: Long, userId: Long): Lesson = {
    val slideSet = getById(slideSetId)
      .getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$slideSetId"))

    tincanPackageService.getActivity(slideSet.activityId) match {
      case Some(activity) => lessonService.getLessonRequired(activity.lessonId)
      case None =>
        val lesson = lessonService.create(
          LessonType.Tincan,
          slideSet.courseId,
          slideSet.title,
          slideSet.description,
          userId,
          slideSet.scoreLimit
        )
        tincanPackageService.addActivities(Seq(TincanActivity(
          lesson.id,
          slideSet.activityId,
          "http://adlnet.gov/expapi/activities/course",
          slideSet.title,
          slideSet.description,
          launch = Some("index.html"),
          resource = None
        )))

        lesson
    }
  }

  override def publishSlideSet(servletContext: ServletContext, id: Long, userId: Long, courseId: Long): Unit = {
    val slideSet = getById(id).getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$id"))

    slideSetRepository.getByActivityId(slideSet.activityId)
      .filter(_.status == SlideSetStatus.Published)
      .filterNot(_.id == id)
      .foreach(s => update(s.copy(status = SlideSetStatus.Archived)))

    update(slideSet.copy(status = SlideSetStatus.Published, modifiedDate = new DateTime))

    val packageFile = slideSetPublisher.composeTinCanPackage(
      servletContext,
      id,
      slideSet.title,
      slideSet.description)

    val oldLesson = findSlideLesson(id, userId)
    packageCategoryGoalStorage.delete(oldLesson.id)

    val lesson = packageUploadService.upload(
      slideSet.title,
      slideSet.description,
      packageFile,
      courseId,
      userId,
      Some(oldLesson.id),
      slideSet.scoreLimit
    )

    lessonAssetHelper.updatePackageAssetEntry(lesson)

    lessonSocialActivityHelper.addWithSet(
      GroupLocalServiceHelper.getGroup(lesson.courseId).getCompanyId,
      lesson.ownerId,
      courseId = Some(lesson.courseId),
      `type` = Some(PackageActivityType.Published.id),
      classPK = Some(lesson.id),
      createDate = DateTime.now)

    for(logo <- getLogo(id)) {
      lessonService.setLogo (lesson.id, slideSet.logo.get, logo)
    }

    val packageGoals = slideService.getBySlideSetId(id, Some(false))
      .flatMap(_.statementCategoryId)
      .flatMap(uriService.getById(_, TincanURIType.Category))
      .groupBy(identity).map { case (key, items) => (key, items.size) }
      .map{ case (u, size) =>
        LessonCategoryGoal(
          lessonId = lesson.id,
          name = u.content,
          category = u.uri,
          count = size
        )}
      .toSeq

    packageCategoryGoalStorage.add(packageGoals)

    val packageAssetId = lessonAssetHelper.updatePackageAssetEntry(lesson)

    lessonTagService.setTags(packageAssetId, slideSet.tags.map(_.id))

    packageFile.delete()
  }

  override def exportSlideSet(id: Long): FileInputStream = {
    val slideSet = getById(id).getOrElse(throw new IllegalStateException(s"No slideSet exist with id $id"))

    slideSetExporter.exportItems(Seq(slideSet))
  }

  override def importSlideSet(stream: InputStream, scopeId: Int): Unit =
    slideSetImporter.importItems(stream, scopeId)

  override def update(slideSetModel: SlideSetModel, tags: Seq[String] = Seq()): SlideSetModel = {
    val hasTheme = slideSetModel.themeId.map(slideThemeRepository.isExist).getOrElse(true)
    val slideSet = if (!hasTheme) slideSetModel.copy(themeId = None) else slideSetModel

    updateTags(slideSetModel, (tags ++ slideSet.tags.map(_.id.toString)).distinct)

    slideSetRepository.update(slideSet)
  }

  override def updateWithVersion(slideSetModel: SlideSetModel, tags: Seq[String]): Option[SlideSetModel] = {
    slideSetRepository.getById(slideSetModel.id.get) map  { s =>
      val list = slideSetRepository.getByActivityId(s.activityId)
      val isPublished = s.status == SlideSetStatus.Published || s.status == SlideSetStatus.Archived
      if (isPublished && list.exists(_.status == SlideSetStatus.Draft)) {
        list.filter(_.status == SlideSetStatus.Draft).foreach(x => delete(x.id.get))
      }
      val maxVersion = slideSetRepository.getByActivityId(slideSetModel.activityId).map(_.version).max
      val newSlideSet = if (isPublished) {
        slideSetModel.copy(status = SlideSetStatus.Draft, version = (math floor (maxVersion + 0.1) * 100) / 100)
      }
      else {
        slideSetModel.copy(status = SlideSetStatus.Draft, version = s.version)
      }
      update(newSlideSet, tags)
    }
  }

  override def create(slideSetModel: SlideSetModel, tags: Seq[String] = Seq()): SlideSetModel = {
    val slideSet = slideSetRepository.create(slideSetModel)
    updateTags(slideSet, tags)
    slideSet
  }

  override def createWithDefaultSlide(slideSetModel: SlideSetModel, tags: Seq[String] = Seq()): SlideSetModel = {
    val slideSet = slideSetRepository.create(slideSetModel.copy(activityId = createNewActivityId(slideSetModel.courseId)))
    updateTags(slideSet, tags)
    slideService.create(
      SlideModel(
        title = "Page 1",
        slideSetId = slideSet.id.get
      )
    )
    slideSet
  }

  override def getVersions(id: Long): List[SlideSetModel] = {
    val slideSet = getById(id).getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$id"))
    slideSetRepository.getByVersion(slideSet.activityId, slideSet.version) map toSlideSetShortModel

  }

  override def deleteAllVersions(id: Long): Unit = {
    val slideSet = getById(id).getOrElse(throw new IllegalStateException(s"There is no slideSet with id=$id"))
    if (slideSet.activityId.nonEmpty) {
      slideSetRepository.getByActivityId(slideSet.activityId).foreach { slideSet =>
        slideSetRepository.delete(slideSet.id.get)
        slideAssetHelper.deleteSlideAsset(slideSet.id.get)
      }
    }
    else {
      slideSetRepository.delete(slideSet.id.get)
    }
  }

  private def toSlideSetShortModel(from: SlideSetEntity): SlideSetModel =
    toSlideSetShortModel(from, slideAssetHelper.getSlideAssetCategories(from.id.get))

  private def toSlideSetShortModel(from: SlideSetEntity, tags: Seq[ValamisTag]): SlideSetModel = {
    val slidesCount = from.id.map(slideService.countBySlideSet(_)) getOrElse 0L
    toSlideSetShortModel(from, slidesCount, tags)
  }

  private def toSlideSetShortModel(from: SlideSetEntity, count: Long, tags: Seq[ValamisTag]): SlideSetModel =
    slideSetModelConversion(from, Nil, Some(count), tags)

  override def createNewActivityId(courseId: Long): String = {
    val uriType = TincanURIType.Course
    val id = UUID.randomUUID.toString
    val companyId = courseService.getById(courseId).map(g => g.getCompanyId)
    val prefix = uriService.getLocalURL(companyId = companyId)
    s"$prefix$uriType/${uriType}_$id"
  }

  private def updateTags(slideSet: SlideSetModel, tags: Seq[String]): Unit = {
    val tagIds = slideTagService.getOrCreateTagIds(tags, CompanyHelper.getCompanyId)
    val assetId = slideAssetHelper.updateSlideAsset(slideSet, None)
    slideTagService.setTags(assetId, tagIds)
  }

  private implicit def convertToModel(from: SlideSetEntity): SlideSetModel =
    slideSetModelConversion(
      from,
      slideService.getBySlideSetId(from.id.get, None),
      tags = slideAssetHelper.getSlideAssetCategories(from.id.get))

  private implicit def convertToModelList(from: List[SlideSetEntity]): List[SlideSetModel] = from.map(convertToModel)

  private implicit def convertToModelOption(from: Option[SlideSetEntity]): Option[SlideSetModel] = from.map(convertToModel)

}