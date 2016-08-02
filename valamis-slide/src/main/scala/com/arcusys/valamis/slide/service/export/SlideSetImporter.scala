package com.arcusys.valamis.slide.service.export

import java.io.{File, InputStream}

import com.arcusys.valamis.content.model.{Answer, Category, PlainText, Question}
import com.arcusys.valamis.content.service.{CategoryService, PlainTextService, QuestionService}
import com.arcusys.valamis.util.export.ImportProcessor
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.{SlideElementServiceContract, SlideServiceContract, SlideSetServiceContract}
import com.arcusys.valamis.slide.storage.SlideElementPropertyRepository
import com.arcusys.valamis.util.FileSystemUtil
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

trait SlideSetImporterContract {
  def importItems(stream: InputStream, scopeId: Int): Unit
}

class SlideSetImporter(implicit val bindingModule: BindingModule)
  extends Injectable
  with SlideSetExportUtils
  with ImportProcessor[ExportFormat]
  with SlideSetImporterContract {
  implicit val jsonFormats: Formats = DefaultFormats

  private lazy val slideSetService = inject[SlideSetServiceContract]
  private val slideService = inject[SlideServiceContract]
  private val slideElementService = inject[SlideElementServiceContract]
  protected val fileService = inject[FileService]
  protected val questionService = inject[QuestionService]
  protected val plainTextService = inject[PlainTextService]
  private lazy val slideElementPropertyRepository = inject[SlideElementPropertyRepository]
  private lazy val categoryService = inject[CategoryService]

  private def addSlides(slides: List[SlideModel],
                        oldSlideSet: SlideSetModel,
                        createdSlideSet: SlideSetModel,
                        slideSetVersion: Option[String],
                        slidesMapper: scala.collection.mutable.Map[Long, Long],
                        localPath: String): Unit = {

    def addSlide(prevSlideModel: SlideModel,
                 title: String,
                 bgColor: Option[String],
                 bgImage: Option[String],
                 font: Option[String],
                 questionFont: Option[String],
                 answerFont: Option[String],
                 answerBg: Option[String],
                 duration: Option[String],
                 leftSlideId: Option[Long],
                 topSlideId: Option[Long],
                 isTemplate: Boolean,
                 isLessonSummary: Boolean,
                 playerTitle: Option[String],
                 properties: Seq[SlideProperties]) = {

      val createdSlide = slideService.create(
        SlideModel(
          title = title,
          bgColor = bgColor,
          bgImage = bgImage,
          font = font,
          questionFont = questionFont,
          answerFont = answerFont,
          answerBg = answerBg,
          duration = duration,
          leftSlideId = leftSlideId,
          topSlideId = topSlideId,
          slideSetId = createdSlideSet.id.get,
          isTemplate = isTemplate,
          isLessonSummary = isLessonSummary,
          playerTitle = playerTitle,
          properties = properties
        )
      )
      slidesMapper += (prevSlideModel.id.get -> createdSlide.id.get)
      bgImage.flatMap{image =>
        val bg = if (image.contains("/delegate/"))
            image.replaceFirst( """.+file=""", "").replaceAll( """(&date=\d+)?"?\)?(\s+.+)?""", "")
          else image
        getFromPath(bg, SlideSetHelper.filePathPrefix(prevSlideModel, slideSetVersion))}.map {
        case (folderName, rawFileName) =>
          val fileName = rawFileName.split(" ").head
          val displayMode = SlideSetHelper.getDisplayMode(bgImage.get)
          val url = addImageToFileService(
            createdSlide,
            slideSetVersion,
            fileName,
            localPath + File.separator + getPath(folderName, fileName, slideSetVersion)
          ) + ' ' + displayMode

          slideService.update(
            SlideModel(
              createdSlide.id,
              title,
              bgColor,
              Some(url),
              font,
              questionFont,
              answerFont,
              answerBg,
              duration,
              leftSlideId,
              topSlideId,
              List(),
              createdSlideSet.id.get,
              createdSlide.statementVerb,
              createdSlide.statementObject,
              createdSlide.statementCategoryId,
              isTemplate,
              isLessonSummary,
              playerTitle,
              properties
            )
          )
      }
    }
    val firstSlide =
      slides
        .find(slide => slide.topSlideId.isEmpty && slide.leftSlideId.isEmpty)
        .getOrElse(throw new IllegalStateException("There should exist a slide without left & top slides"))

    addSlide(
      firstSlide,
      firstSlide.title,
      firstSlide.bgColor,
      firstSlide.bgImage,
      firstSlide.font,
      firstSlide.questionFont,
      firstSlide.answerFont,
      firstSlide.answerBg,
      firstSlide.duration,
      firstSlide.leftSlideId,
      firstSlide.topSlideId,
      firstSlide.isTemplate,
      firstSlide.isLessonSummary,
      firstSlide.playerTitle,
      firstSlide.properties)

    slides.find(_.leftSlideId == firstSlide.id).foreach(addSlidesHelper)
    slides.find(_.topSlideId == firstSlide.id).foreach(addSlidesHelper)

    def addSlidesHelper(slide: SlideModel): Unit = {
      addSlide(
        slide,
        slide.title,
        slide.bgColor,
        slide.bgImage,
        slide.font,
        slide.questionFont,
        slide.answerFont,
        slide.answerBg,
        slide.duration,
        slide.leftSlideId.flatMap(oldLeftSlideId => slidesMapper.get(oldLeftSlideId)),
        slide.topSlideId.flatMap(oldTopSlideId => slidesMapper.get(oldTopSlideId)),
        slide.isTemplate,
        slide.isLessonSummary,
        slide.playerTitle,
        slide.properties)

      slides.find(_.leftSlideId == slide.id).foreach(addSlidesHelper)
      slides.find(_.topSlideId == slide.id).foreach(addSlidesHelper)
    }
  }

  private def addSlideElements(element: SlideElementModel,
                               questions: List[(Question, Seq[Answer])],
                               plaintexts: List[PlainText],
                               newSlideId: Long,
                               slideSetVersion: Option[String],
                               courseId: Long,
                               localPath: String,
                               data: String): Unit = {

      val planTextFromQuestions = plaintexts.filter {
        _.id match {
          case Some(id) if id.toString == element.content => true
          case _ => false
        }
      }


      val slideElement = if (planTextFromQuestions.nonEmpty && (element.slideEntityType == SlideEntityType.Question))
        element.copy(slideEntityType = SlideEntityType.PlainText)
      else element

      slideElement.slideEntityType match {
        case SlideEntityType.Image | SlideEntityType.Pdf | SlideEntityType.Video | SlideEntityType.Webgl =>

          val (slideContent, folder) =
            if (slideElement.content.contains("/delegate/")) {
              val content = slideElement.content.replaceFirst( """.+file=""", "").replaceAll( """(&date=\d+)?"?\)?(\s+.+)?""", "")
              val folderId = slideElement.content.replaceFirst( """.+folderId=""", "").replaceAll( """(&file=.+)?"?\)?(\s+.+)?""", "")

              if (folderId.isEmpty)
                (content, SlideSetHelper.filePathPrefix(slideElement, slideSetVersion))
              else
                (content, folderId + "/")
            }
            else {
              (slideElement.content, SlideSetHelper.filePathPrefix(slideElement, slideSetVersion))
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
                slideSetVersion,
                fileName,
                path.get,
                newSlideId
              )

              val content =
                if (slideElement.slideEntityType == SlideEntityType.Pdf)
                  slideElement.content.replaceFirst("(.+(slide|quiz)Data)\\d+(/.+)", "$1" + createdSlideElement.id.get + "$3")
                else
                  url

              slideElementService.update(
                SlideElementModel(
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
          val (question, answers) = questions
            .find(qt => qt._1.id.contains(slideElement.content.toLong))
            .getOrElse(throw new IllegalStateException("No question with required id: " + slideElement.content.toLong))
          val categoryId = question.categoryId
          val categoryNewId: Option[Long] = categoryId.map { id =>
            categoryService.getByTitleAndCourseId(id.toString, courseId) match {
              case Some(cat) => cat.id.get
              case _ =>
                val newId = categoryService.create(Category(None, id.toString, "", None, question.courseId)).id.get
                categoryService.moveToCourse(newId, courseId, true)
                newId
            }
          }

          val createdQuestion = questionService.createWithNewCategory(question, answers, categoryNewId)
          questionService.moveToCourse(createdQuestion.id.get, courseId.toInt, moveToRoot = false)
          //TODO encode/decode questions (and plaintext?) ???
          //questionServiceOld.decodeQuestion(createdQuestion)
          createSlideElement(
            slideElement,
            createdQuestion.id.get.toString,
            newSlideId,
            data)
        case SlideEntityType.PlainText =>
          val plaintext = plaintexts
            .find(_.id.contains(slideElement.content.toLong))
            .getOrElse(throw new IllegalStateException("No plaintext with required id: " + slideElement.content.toLong))
          val createdPlaintext = plainTextService.create(plaintext)
          plainTextService.moveToCourse(createdPlaintext.id.get, courseId.toInt, moveToRoot = true)
          createSlideElement(
            slideElement,
            createdPlaintext.id.get.toString,
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

  private def createSlideElement(slideElement: SlideElementModel,
                                 content: String,
                                 slideId: Long,
                                 data: String): SlideElementModel = {
    val newSlideElement = slideElementService.create(
      SlideElementModel(
        None,
        slideElement.zIndex,
        content,
        slideElement.slideEntityType,
        slideId,
        slideElement.correctLinkedSlideId,
        slideElement.incorrectLinkedSlideId,
        slideElement.notifyCorrectAnswer,
        slideElement.properties
      )
    )
    if (slideElement.properties.isEmpty){

      val oldElements = for {
        slide <- parse(data).\("slideSet").\("slides").children
        slideElement <- slide.\("slideElements").extract[List[SlideOldElementModel]]
      } yield slideElement

      oldElements
        .filter(_.id == slideElement.id.get)
        .foreach(el =>
        slideElementPropertyRepository.createFromOldValues(
          deviceId = 1,
          newSlideElement.id.get,
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

    val slideSet = item.slideSet
    val version = item.version
    val (questions, plaintexts) = version match {
      case Some("2.1") => (item.questions.map(QuestionExternalFormat.importQuestion),
        item.plaintexts.map(QuestionExternalFormat.importPlainText))
      case _ =>
        val planText = item.questions.filter(q => (q.tpe == 8)||(q.tpe == 9))
          .map{
            QuestionExternalFormat.importPlainTextLast

          }

        val newQuestions = item.questions.filter(q => (q.tpe != 8)&&(q.tpe != 9))
          .map(QuestionExternalFormat.importQuestionLast)
        (newQuestions, planText)
    }

    val topDownNavigation = slideSet.slides.exists(_.topSlideId.isDefined)

    val createdSlideSet = slideSetService.create(
      SlideSetModel(
        None,
        slideSet.title,
        slideSet.description,
        courseId,
        slideSet.logo,
        List(),
        slideSet.isTemplate,
        slideSet.isSelectedContinuity,
        None,
        slideSet.duration,
        slideSet.scoreLimit,
        slideSet.playerTitle,
        None,
        topDownNavigation,
        slideSetService.createNewActivityId(courseId)),
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
      addImageToFileService(createdSlideSet, version, logoString, path)
    }

    val slideMapper = scala.collection.mutable.Map[Long, Long]()

    addSlides(
      slideSet.slides,
      slideSet,
      createdSlideSet,
      version,
      slideMapper,
      tempDirectory.getPath)

    slideMapper.foreach { case (oldSlideId, newSlideId) =>
      for {
        slide <- slideSet.slides.filter(_.id == Some(oldSlideId))
        slideElement <- slide.slideElements
      } {
        val correctLinkedSlideId = slideElement.correctLinkedSlideId.flatMap(oldId => slideMapper.get(oldId))
        val incorrectLinkedSlideId = slideElement.incorrectLinkedSlideId.flatMap(oldId => slideMapper.get(oldId))
        addSlideElements(slideElement.copy(correctLinkedSlideId = correctLinkedSlideId, incorrectLinkedSlideId = incorrectLinkedSlideId),
          questions,
          plaintexts,
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
}