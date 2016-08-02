package com.arcusys.learn.liferay.update.version250.slide

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import scala.slick.driver.JdbcProfile

trait SlideTableComponent {
  protected val driver: JdbcProfile

  import driver.simple._

  case class SlideSet(id: Option[Long] = None,
                      title: String,
                      description: String,
                      courseId: Long,
                      logo: Option[String] = None,
                      isTemplate: Boolean = false,
                      isSelectedContinuity: Boolean = false,
                      themeId: Option[Long] = None)

  class SlideSetTable(tag: Tag) extends Table[SlideSet](tag, tblName("SLIDE_SET")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION", O.DBType(varCharMax))
    def courseId = column[Long]("COURSE_ID")
    def logo = column[Option[String]]("LOGO")
    def isTemplate = column[Boolean]("IS_TEMPLATE")
    def isSelectedContinuity = column[Boolean]("IS_SELECTED_CONTINUITY")
    def themeId = column[Option[Long]]("THEME_ID")

    def * = (id.?, title, description, courseId, logo, isTemplate, isSelectedContinuity, themeId) <>(SlideSet.tupled, SlideSet.unapply)

    def slideThemeFK = foreignKey(fkName("SLIDESET_TO_THEME"), themeId, slideThemes)(x => x.id)
  }

  case class Slide(id: Option[Long] = None,
                   title: String,
                   bgColor: Option[String] = None,
                   bgImage: Option[String] = None,
                   font: Option[String] = None,
                   questionFont: Option[String] = None,
                   answerFont: Option[String] = None,
                   answerBg: Option[String] = None,
                   duration: Option[String] = None,
                   leftSlideId: Option[Long] = None,
                   topSlideId: Option[Long] = None,
                   slideSetId: Long,
                   statementVerb: Option[String] = None,
                   statementObject: Option[String] = None,
                   statementCategoryId: Option[String] = None,
                   isTemplate: Boolean = false,
                   isLessonSummary: Boolean = false)

  class SlideTable(tag: Tag) extends Table[Slide](tag, tblName("SLIDE")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def bgColor = column[Option[String]]("BG_COLOR")
    def bgImage = column[Option[String]]("BG_IMAGE")
    def font = column[Option[String]]("FONT")
    def questionFont = column[Option[String]]("QUESTION_FONT")
    def answerFont = column[Option[String]]("ANSWER_FONT")
    def answerBg = column[Option[String]]("ANSWER_BG")
    def duration = column[Option[String]]("DURATION")
    def leftSlideId = column[Option[Long]]("LEFT_SLIDE_ID")
    def topSlideId = column[Option[Long]]("TOP_SLIDE_ID")
    def slideSetId = column[Long]("SLIDE_SET_ID")
    def statementVerb = column[Option[String]]("STATEMENT_VERB")
    def statementObject = column[Option[String]]("STATEMENT_OBJECT")
    def statementCategoryId = column[Option[String]]("STATEMENT_CATEGORY_ID")
    def isTemplate = column[Boolean]("IS_TEMPLATE")
    def isLessonSummary = column[Boolean]("IS_LESSON_SUMMARY")

    def * = (
      id.?,
      title,
      bgColor,
      bgImage,
      font,
      questionFont,
      answerFont,
      answerBg,
      duration,
      leftSlideId,
      topSlideId,
      slideSetId,
      statementVerb,
      statementObject,
      statementCategoryId,
      isTemplate,
      isLessonSummary) <>(Slide.tupled, Slide.unapply)

    def slideSetFK = foreignKey(fkName("SLIDE_TO_SLIDESET"), slideSetId, slideSets)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  case class SlideElement(id: Option[Long] = None,
                          top: String,
                          left: String,
                          width: String,
                          height: String,
                          zIndex: String,
                          content: String,
                          slideEntityType: String,
                          slideId: Long,
                          correctLinkedSlideId: Option[Long] = None,
                          incorrectLinkedSlideId: Option[Long] = None,
                          notifyCorrectAnswer: Option[Boolean] = None)


  class SlideElementTable(tag: Tag) extends Table[SlideElement](tag, tblName("SLIDE_ELEMENT")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def top = column[String]("TOP")
    def left = column[String]("LEFT")
    def width = column[String]("WIDTH")
    def height = column[String]("HEIGHT")
    def zIndex = column[String]("Z_INDEX")
    def content = column[String]("CONTENT", O.DBType(varCharMax))
    def slideEntityType = column[String]("SLIDE_ENTITY_TYPE")
    def slideId = column[Long]("SLIDE_ID")
    def correctLinkedSlideId = column[Option[Long]]("CORRECT_LINKED_SLIDE_ID")
    def incorrectLinkedSlideId = column[Option[Long]]("INCORRECT_LINKED_SLIDE_ID")
    def notifyCorrectAnswer = column[Option[Boolean]]("NOTIFY_CORRECT_ANSWER")

    def * = (
      id.?,
      top,
      left,
      width,
      height,
      zIndex,
      content,
      slideEntityType,
      slideId,
      correctLinkedSlideId,
      incorrectLinkedSlideId,
      notifyCorrectAnswer) <>(SlideElement.tupled, SlideElement.unapply)

    def slideFK = foreignKey(fkName("SLIDE_ELEMENT_TO_SLIDE"), slideId, slides)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  case class SlideTheme(id: Option[Long] = None,
                        title: String = "Theme",
                        bgColor: Option[String] = None,
                        bgImage: Option[String] = None,
                        font: Option[String] = None,
                        questionFont: Option[String] = None,
                        answerFont: Option[String] = None,
                        answerBg: Option[String] = None,
                        userId: Option[Long] = None,
                        isDefault: Boolean = false)

  class SlideThemeTable(tag : Tag) extends Table[SlideTheme](tag, tblName("SLIDE_THEME")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE", O.NotNull)
    def bgColor = column[Option[String]]("BGCOLOR")
    def bgImage = column[Option[String]]("BGIMAGE")
    def font = column[Option[String]]("FONT")
    def questionFont = column[Option[String]]("QUESTIONFONT")
    def answerFont = column[Option[String]]("ANSWERFONT")
    def answerBg = column[Option[String]]("ANSWERBG")
    def userId = column[Option[Long]]("USER_ID")
    def isDefault = column[Boolean]("IS_DEFAULT", O.Default(false))

    def * = (
      id.?,
      title,
      bgColor,
      bgImage,
      font,
      questionFont,
      answerFont,
      answerBg,
      userId,
      isDefault)<>(SlideTheme.tupled, SlideTheme.unapply)
  }

  val slideSets = TableQuery[SlideSetTable]
  val slides = TableQuery[SlideTable]
  val slideElements = TableQuery[SlideElementTable]
  val slideThemes = TableQuery[SlideThemeTable]
  }
