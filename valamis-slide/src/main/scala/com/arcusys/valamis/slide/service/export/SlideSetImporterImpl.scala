package com.arcusys.valamis.slide.service.export

import java.io.{File, InputStream}

import com.arcusys.valamis.content.model.{Answer, Category, PlainText, Question}
import com.arcusys.valamis.content.service.CategoryService
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.{SlideElementService, SlideService, SlideSetService}
import com.arcusys.valamis.slide.storage.SlideElementPropertyRepository
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.util.export.ImportProcessor
import com.arcusys.valamis.util.serialization.DateTimeSerializer
import org.joda.time.DateTime
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

trait SlideSetImporter {
  def importItems(stream: InputStream, scopeId: Int): Unit
}

abstract class SlideSetImporterImpl
  extends SlideSetExportUtils
  with ImportProcessor[ExportFormat]
  with SlideSetImporter {
  implicit val jsonFormats: Formats = DefaultFormats + DateTimeSerializer

  def slideSetService: SlideSetService
  def slideService: SlideService
  def slideElementService: SlideElementService
  def slideElementPropertyRepository: SlideElementPropertyRepository
  def categoryService: CategoryService

  private def addSlides(slides: Seq[Slide],
                        oldSlideSet: SlideSet,
                        createdSlideSet: SlideSet,
                        slideSetVersion: Option[String],
                        slidesMapper: scala.collection.mutable.Map[Long, Long],
                        localPath: String): Unit = {

    val firstSlide = slideService.getRootSlide(slides)

    firstSlide foreach { slide =>
      addSlide(slide, slide.leftSlideId,slide.topSlideId)
      slides.find(_.leftSlideId.contains(slide.id)).foreach(addSlidesHelper)
      slides.find(_.topSlideId.contains(slide.id)).foreach(addSlidesHelper)
    }

    def addSlide(prevSlideModel: Slide, leftSlideId: Option[Long], topSlideId: Option[Long]) = {

      val createdSlide = slideService.create(
        prevSlideModel.copy(
          id = 0L,
          slideSetId = createdSlideSet.id,
          leftSlideId = leftSlideId,
          topSlideId = topSlideId)
      )
      slidesMapper += (prevSlideModel.id -> createdSlide.id)
      updateBgImage(prevSlideModel, slideSetVersion, createdSlide, localPath)
    }

    def addSlidesHelper(slide: Slide): Unit = {
      addSlide(
        slide,
        slide.leftSlideId.flatMap(oldLeftSlideId => slidesMapper.get(oldLeftSlideId)),
        slide.topSlideId.flatMap(oldTopSlideId => slidesMapper.get(oldTopSlideId))
      )
      slides.find(_.leftSlideId.contains(slide.id)).foreach(addSlidesHelper)
      slides.find(_.topSlideId.contains(slide.id)).foreach(addSlidesHelper)
    }
  }

  private def updateBgImage(prevSlideModel: Slide,
                            slideSetVersion: Option[String],
                            createdSlide: Slide,
                            localPath: String) = {
    prevSlideModel.bgImage.flatMap {image =>
      val bg = if (image.contains("/delegate/")) {
        image.replaceFirst( """.+file=""", "").replaceAll( """(&date=\d+)?"?\)?(\s+.+)?""", "")
      }
      else {
        image
      }
      getFromPath(bg, SlideSetHelper.filePathPrefix(prevSlideModel))}.foreach {
      case (folderName, rawFileName) =>
        val fileName = rawFileName.split(" ").head
        val displayMode = SlideSetHelper.getDisplayMode(prevSlideModel.bgImage.get)
        val url = addImageToFileService(
          createdSlide,
          fileName,
          localPath + File.separator + getPath(folderName, fileName, slideSetVersion)
        ) + ' ' + displayMode

        slideService.updateBgImage(createdSlide.id, Some(url))
    }
  }

  private def addSlideElements(element: SlideElement,
                               questions: Seq[(Question, Seq[Answer], Option[Long])],
                               plaintexts: Seq[(PlainText, Option[Long])],
                               newSlideId: Long,
                               slideSetVersion: Option[String],
                               courseId: Long,
                               localPath: String,
                               data: String): Unit = {

      val planTextFromQuestions = plaintexts.filter {
        _._1.id match {
          case Some(id) if id.toString == element.content => true
          case _ => false
        }
      }

      val slideElement = if (planTextFromQuestions.nonEmpty && (element.slideEntityType == SlideEntityType.Question))
        element.copy(slideEntityType = SlideEntityType.PlainText)
      else element

      slideElement.slideEntityType match {
        case SlideEntityType.Image | SlideEntityType.Pdf | SlideEntityType.Video | SlideEntityType.Webgl | SlideEntityType.Audio =>

          val (slideContent, folder) =
            if (slideElement.content.contains("/delegate/")) {
              val content = slideElement.content.replaceFirst( """.+file=""", "").replaceAll( """(&date=\d+)?"?\)?(\s+.+)?""", "")
              val folderId = slideElement.content.replaceFirst( """.+folderId=""", "").replaceAll( """(&file=.+)?"?\)?(\s+.+)?""", "")

              if (folderId.isEmpty)
                (content, SlideSetHelper.filePathPrefix(slideElement))
              else
                (content, folderId + "/")
            }
            else {
              (slideElement.content, SlideSetHelper.filePathPrefix(slideElement))
            }

          val createdSlideElement = createSlideElement(slideElement, slideContent, newSlideId, data)

          getFromPath(slideContent, folder).foreach { case (folderName: String, rawFileName: String) =>
            val fileName = rawFileName.split(" ").head

            val realFilePath = localPath + File.separator + getPath(folderName, fileName, slideSetVersion)
            lazy val ext = slideElement.content.replaceFirst( """.+ext=""", "").replaceAll( """\"\)""", "")

            val path = Seq(realFilePath, realFilePath + "." + ext).find(new File(_).exists)

            if (path.isDefined) {
              val url = addImageToFileService(
                createdSlideElement,
                fileName,
                path.get
              )

              val content =
                if (slideElement.slideEntityType == SlideEntityType.Pdf)
                  slideElement.content.replaceFirst("(.+(slide|quiz)Data)\\d+(/.+)", "$1" + createdSlideElement.id + "$3")
                else
                  url

              slideElementService.update(
                SlideElement(
                  createdSlideElement.id,
                  slideElement.zIndex,
                  content,
                  slideElement.slideEntityType,
                  newSlideId,
                  slideElement.correctLinkedSlideId,
                  slideElement.incorrectLinkedSlideId,
                  slideElement.notifyCorrectAnswer,
                  slideElement.properties)
              )
            }
          }
        case SlideEntityType.Question =>
          createSlideElement(
            slideElement,
            questions.find(_._1.id == Some(slideElement.content.toLong))
              .flatMap(_._3)
              .map(_.toString).getOrElse(""),
            newSlideId,
            data)
        case SlideEntityType.PlainText =>
          createSlideElement(
            slideElement,
            plaintexts.find(_._1.id == Some(slideElement.content.toLong))
              .flatMap(_._2)
              .map(_.toString).getOrElse(""),
            newSlideId,
            data)
        case SlideEntityType.RandomQuestion =>
          val newContent = slideElement.content
            .split(",")
            .map {
              case e if e.startsWith(SlideConstants.PlainTextIdPrefix) =>
                plaintexts
                  .find(_._1.id == Some(e.replace(SlideConstants.PlainTextIdPrefix, "").toLong))
                  .flatMap(_._2)
                  .map(SlideConstants.PlainTextIdPrefix + _.toString)
              case e if e.startsWith(SlideConstants.QuestionIdPrefix) =>
                questions
                  .find(_._1.id == Some(e.replace(SlideConstants.QuestionIdPrefix, "").toLong))
                  .flatMap(_._3)
                  .map(SlideConstants.QuestionIdPrefix + _.toString)
              case e => throw new IllegalStateException("No object in random question with required id: " + e)
            }
          createSlideElement(
            slideElement,
            newContent.flatten.mkString(","),
            newSlideId,
            data)

        case _ =>
          createSlideElement(
            slideElement,
            slideElement.content,
            newSlideId,
            data)
      }
  }

  private def createSlideElement(slideElement: SlideElement,
                                 content: String,
                                 slideId: Long,
                                 data: String): SlideElement = {
    val newSlideElement = slideElementService.create(slideElement.copy(content = content, slideId = slideId))
    //if in old packages elements without properties
    if (slideElement.properties.isEmpty){
      val oldElements = for {
        slide <- parse(data).\("slideSet").\("slides").children
        slideElement <- slide.\("slideElements").extract[List[SlideOldElementModel]]
      } yield slideElement

      oldElements
        .filter(_.id == slideElement.id)
        .foreach(el =>
        slideElementPropertyRepository.createFromOldValues(
          deviceId = 1,
          newSlideElement.id,
          el.top,
          el.left,
          el.width,
          el.height)
      )
    }
    newSlideElement
  }

  override protected def importItems(items: List[ExportFormat],
                                     courseId: Long,
                                     tempDirectory: File,
                                     userId: Long,
                                     data: String): Unit = {
    require(items.length == 1)
    val item = items.head

    val slideSet = item.slideSet.toModel
    val version = item.version
    val (questions, plaintexts, categories) = version match {
      case Some("2.1") => (item.questions.map(QuestionExternalFormat.importQuestion),
        item.plaintexts.map(QuestionExternalFormat.importPlainText),
        item.categories.map(QuestionExternalFormat.importCategory))
      case _ =>
        val planText = item.questions.filter(q => (q.tpe == 8)||(q.tpe == 9))
          .map(QuestionExternalFormat.importPlainTextLast)

        val newQuestions = item.questions.filter(q => (q.tpe != 8)&&(q.tpe != 9))
          .map(QuestionExternalFormat.importQuestionLast)
        (newQuestions, planText, Seq[Category]())
    }

    val (questionMap, plainTextMap) = if (questions.size + plaintexts.size > 0) {
      val rootCategory = categoryService.create(
        Category(None,
          s"Export for ${item.slideSet.title}",
          s"Export for ${item.slideSet.title} from ${DateTime.now}",
          None,
          courseId
        )
      )
      val categoryMap = categories.map { cat =>
        val oldCatId = cat.id
        val newCatId = categoryService.create(cat.copy(categoryId = rootCategory.id)).id
        (oldCatId -> newCatId)
      }
      val qMap = questions.map { qPair =>
        val newQuestion = questionService.createWithNewCategory(qPair._1,
          qPair._2,
          categoryMap.find(_._1 == qPair._1.categoryId).flatMap(_._2)
        )
        (qPair._1, qPair._2, newQuestion.id)
      }

      val pMap = plaintexts.map { pt =>
        val newPT = plainTextService.create(pt.copy(categoryId = categoryMap.find(_._1 == pt.categoryId).flatMap(_._2)))
        (pt -> newPT.id)
      }

      categoryService.moveToCourse(rootCategory.id.get, courseId, true)
      (qMap, pMap)
    } else {
      (Seq(), Seq())
    }

    val topDownNavigation = item.slideSet.slides.exists(_.topSlideId.isDefined)

    val createdSlideSet = slideSetService.create(
      SlideSet(
        title = slideSet.title,
        description = slideSet.description,
        courseId = courseId,
        logo = slideSet.logo,
        isTemplate = slideSet.isTemplate,
        isSelectedContinuity = slideSet.isSelectedContinuity,
        duration = slideSet.duration,
        scoreLimit = slideSet.scoreLimit,
        playerTitle = slideSet.playerTitle,
        topDownNavigation = topDownNavigation,
        activityId = slideSetService.createNewActivityId(courseId),
        requiredReview = slideSet.requiredReview),
      Seq()
    )
    slideSet.logo.map { logoString =>
      val folderPrefix = version match {
        case Some(v) => "resources"
        case _ => "images"
      }
      val path =
        tempDirectory.getPath +
          File.separator +
          folderPrefix +
          File.separator +
          SlideSetHelper.filePathPrefix(slideSet, version) +
          File.separator +
          logoString
      addImageToFileService(createdSlideSet, logoString, path)
    }

    val slideMapper = scala.collection.mutable.Map[Long, Long]()

    addSlides(item.slideSet.slides, slideSet, createdSlideSet, version, slideMapper, tempDirectory.getPath)

    slideMapper.foreach { case (oldSlideId, newSlideId) =>
      for {
        slide <- item.slideSet.slides.filter(_.id == oldSlideId)
        slideElement <- slide.slideElements
      } {
        val correctLinkedSlideId = slideElement.correctLinkedSlideId.flatMap(oldId => slideMapper.get(oldId))
        val incorrectLinkedSlideId = slideElement.incorrectLinkedSlideId.flatMap(oldId => slideMapper.get(oldId))
        addSlideElements(slideElement.copy(correctLinkedSlideId = correctLinkedSlideId, incorrectLinkedSlideId = incorrectLinkedSlideId),
          questionMap,
          plainTextMap,
          newSlideId,
          version,
          courseId,
          tempDirectory.getPath,
          data)
      }
    }
  }

  override def importItems(stream: InputStream, scopeId: Int): Unit =
    importItems(FileSystemUtil.streamToTempFile(stream, "Import", ".zip"), scopeId)

  implicit class SlideModelExtension(val slideSetExport: SlideSetExportModel) {
    def toModel: SlideSet = {
      SlideSet(
        id = slideSetExport.id,
        title = slideSetExport.title,
        description = slideSetExport.description,
        courseId = slideSetExport.courseId,
        logo = slideSetExport.logo,
        isTemplate = slideSetExport.isTemplate,
        isSelectedContinuity = slideSetExport.isSelectedContinuity,
        themeId = slideSetExport.themeId,
        duration = slideSetExport.duration,
        scoreLimit = slideSetExport.scoreLimit,
        playerTitle = slideSetExport.playerTitle,
        topDownNavigation = slideSetExport.topDownNavigation,
        activityId = slideSetExport.activityId,
        status = slideSetExport.status,
        version = slideSetExport.version,
        modifiedDate = slideSetExport.modifiedDate,
        oneAnswerAttempt = slideSetExport.oneAnswerAttempt,
        requiredReview = slideSetExport.requiredReview.getOrElse(false)
      )
    }
  }
}