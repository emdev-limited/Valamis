package com.arcusys.valamis.slide

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.slide.model._

import scala.slick.driver.JdbcProfile

trait SlideTableComponent {
  protected val driver: JdbcProfile
  import driver.simple._

  class SlideSetTable(tag : Tag) extends Table[SlideSetEntity](tag, tblName("SLIDE_SET")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION", O.DBType(varCharMax))
    def courseId = column[Long]("COURSE_ID")
    def logo = column[Option[String]]("LOGO")
    def isTemplate = column[Boolean]("IS_TEMPLATE")
    def isSelectedContinuity = column[Boolean]("IS_SELECTED_CONTINUITY")
    def themeId = column[Option[Long]]("THEME_ID")
    def duration = column[Option[Long]]("DURATION")
    def scoreLimit = column[Option[Double]]("SCORE_LIMIT")
    def playerTitle = column[String]("PLAYER_TITLE")

    def * = (id.?, title, description, courseId, logo, isTemplate, isSelectedContinuity, themeId, duration, scoreLimit, playerTitle) <> (SlideSetEntity.tupled, SlideSetEntity.unapply)

    def update = (title, description, courseId, logo, isTemplate, isSelectedContinuity, themeId, duration, scoreLimit, playerTitle) <> (SlideSetEntityUpdate.tupled, SlideSetEntityUpdate.unapply)

    def slideThemeFK = foreignKey(fkName("SLIDESET_TO_THEME"), themeId, slideThemes)(x => x.id)
  }

  class SlideTable(tag : Tag) extends Table[SlideEntity](tag, tblName("SLIDE")) {
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
    def playerTitle = column[Option[String]]("PLAYER_TITLE")

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
      isLessonSummary,
      playerTitle) <> (SlideEntity.tupled, SlideEntity.unapply)

    def update = (
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
      isLessonSummary,
      playerTitle) <> (SlideEntityUpdate.tupled, SlideEntityUpdate.unapply)


    def slideSetFK = foreignKey(fkName("SLIDE_TO_SLIDESET"), slideSetId, slideSets)(x => x.id, onDelete = ForeignKeyAction.Cascade)
//    def leftSlideFK = foreignKey(fkName("LEFT_SLIDE_ID"), leftSlideId, slides)(x => x.id, onDelete = ForeignKeyAction.Cascade) UNKOMMENT if needed
//    def topSlideFK = foreignKey(fkName("TOP_SLIDE_ID"), topSlideId, slides)(x => x.id, onDelete = ForeignKeyAction.Cascade) UNKOMMENT if needed
  }

  class SlideElementTable(tag : Tag) extends Table[SlideElementEntity](tag, tblName("SLIDE_ELEMENT")) {
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
      notifyCorrectAnswer) <> (SlideElementEntity.tupled, SlideElementEntity.unapply)

    def update = (
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
      notifyCorrectAnswer) <> (SlideElementEntityUpdate.tupled, SlideElementEntityUpdate.unapply)

    def slideFK = foreignKey(fkName("SLIDE_ELEMENT_TO_SLIDE"), slideId, slides)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  class SlideThemeTable(tag : Tag) extends Table[SlideThemeModel](tag, tblName("SLIDE_THEME")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def bgColor = column[Option[String]]("BGCOLOR")
    def bgImage = column[Option[String]]("BGIMAGE")
    def font = column[Option[String]]("FONT")
    def questionFont = column[Option[String]]("QUESTIONFONT")
    def answerFont = column[Option[String]]("ANSWERFONT")
    def answerBg = column[Option[String]]("ANSWERBG")
    def userId = column[Option[Long]]("USER_ID")
    def isDefault = column[Boolean]("IS_DEFAULT", O.Default(false))

    def * = (id.?, title, bgColor, bgImage, font, questionFont, answerFont, answerBg, userId, isDefault) <> (SlideThemeModel.tupled, SlideThemeModel.unapply)
  }

  val slideSets = TableQuery[SlideSetTable]
  val slides = TableQuery[SlideTable]
  val slideElements = TableQuery[SlideElementTable]
  val slideThemes = TableQuery[SlideThemeTable]
  val slideElementProperties = TableQuery[SlideElementPropertyTable]
  val devices = TableQuery[DeviceTable]
  val slideProperties = TableQuery[SlidePropertyTable]

  // TODO: move default templates out from schema definition
  val defaultSlideThemes = Seq(
    SlideThemeModel(
      title = "Default",
      bgColor = Some("#eaeaea"),
      font = Some("'Helvetica Neue' Helvetica sans-serif$$#1c1c1c$"),
      questionFont = Some("'Helvetica Neue' Helvetica sans-serif$$#1c1c1c$"),
      answerFont = Some("'Open Sans' 'Helvetica Neue' Helvetica sans-serif$$#1c1c1c$"),
      answerBg = Some("#fff"),
      isDefault = true
    ),
    SlideThemeModel(
      title = "Black and White",
      bgColor = Some("#000000"),
      font = Some("ubuntu$20px$#ffffff"),
      isDefault = true
    ),
    SlideThemeModel(
      title = "Blue",
      bgColor = Some("#48aadf"),
      font = Some("roboto$20px$#ffffff"),
      isDefault = true
    ),
    SlideThemeModel(
      title = "Default White",
      bgColor = Some("#f7f7f7"),
      font = Some("ubuntu$20px$#000000"),
      isDefault = true
    ),
    SlideThemeModel(
      title = "Green",
      bgColor = Some("#f7f7f7"),
      font = Some("roboto$20px$#9cc83d"),
      isDefault = true
    )
  )

  val defaultSlideSetTemplate =
    SlideSetEntity(
      courseId = 0L,
      isTemplate = true
    )

  class DeviceTable(tag : Tag) extends Table[DeviceEntity](tag, tblName("DEVICE")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME", O.Length(20, varying = true))
    def minWidth = column[Int]("MIN_WIDTH")
    def maxWidth = column[Int]("MAX_WIDTH")
    def minHeight = column[Int]("MIN_HEIGHT")
    def margin = column[Int]("MARGIN")

    def * = (id.?, name, minWidth, maxWidth, minHeight, margin) <> (DeviceEntity.tupled, DeviceEntity.unapply)
  }

  class SlideElementPropertyTable(tag : Tag) extends Table[SlideElementPropertyEntity](tag, tblName("SLIDE_ELEMENT_PROPERTY")) {
    def slideElementId = column[Long]("SLIDE_ELEMENT_ID")
    def deviceId = column[Long]("DEVICE_ID")
    def key = column[String]("DATA_KEY")
    def value = column[String]("DATA_VALUE")
    def pk = primaryKey("PK_PROPERTY", (slideElementId, deviceId, key))

    def * = (slideElementId, deviceId, key, value) <> (SlideElementPropertyEntity.tupled, SlideElementPropertyEntity.unapply)

    def slideElementFK = foreignKey("PROPERTY_ELEMENT", slideElementId, slideElements)(_.id, onDelete = ForeignKeyAction.Cascade)
    def deviceFK = foreignKey("PROPERTY_DEVICE", deviceId, devices)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  class SlidePropertyTable(tag: Tag) extends Table[SlidePropertyEntity](tag, tblName("SLIDE_PROPERTY")) {
    def slideId = column[Long]("SLIDE_ID")
    def deviceId = column[Long]("DEVICE_ID")
    def key = column[String]("DATA_KEY")
    def value = column[String]("DATA_VALUE")
    def pk = primaryKey("PK_SLIDE_PROPERTY", (slideId, deviceId, key))

    def * = (slideId, deviceId, key, value) <>(SlidePropertyEntity.tupled, SlidePropertyEntity.unapply)

    def slideSetFK = foreignKey(fkName("PROPERTY_TO_SLIDE"), slideId, slides)(_.id, onDelete = ForeignKeyAction.Cascade)
    def deviceFK = foreignKey(fkName("PROPERTY_TO_DEVICE"), deviceId, devices)(_.id, onDelete = ForeignKeyAction.Cascade)
  }
}