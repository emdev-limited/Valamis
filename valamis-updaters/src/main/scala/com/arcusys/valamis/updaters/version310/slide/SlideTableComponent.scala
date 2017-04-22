package com.arcusys.valamis.updaters.version310.slide

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait SlideTableComponent extends TypeMapper{ self: SlickProfile =>

  import driver.api._

  case class SlideSet(id: Long,
                      title: String,
                      description: String,
                      courseId: Long,
                      logo: Option[String] = None,
                      isTemplate: Boolean = false,
                      isSelectedContinuity: Boolean = false,
                      themeId: Option[Long] = None,
                      duration: Option[Long] = None,
                      scoreLimit: Option[Double] = None,
                      playerTitle: String = "page",
                      topDownNavigation: Boolean = false,
                      activityId: String = "",
                      status: String,
                      version: Double = 1.0,
                      modifiedDate: DateTime = new DateTime(),
                      oneAnswerAttempt: Boolean = false,
                      lockUserId: Option[Long] = None,
                      lockDate: Option[DateTime] = None,
                      requiredReview: Boolean = false)

  class SlideSetTable(tag: Tag) extends Table[SlideSet](tag, tblName("SLIDE_SET")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def title = column[String]("TITLE")

    def description = column[String]("DESCRIPTION", O.SqlType(varCharMax))

    def courseId = column[Long]("COURSE_ID")

    def logo = column[Option[String]]("LOGO")

    def isTemplate = column[Boolean]("IS_TEMPLATE")

    def isSelectedContinuity = column[Boolean]("IS_SELECTED_CONTINUITY")

    def themeId = column[Option[Long]]("THEME_ID")

    def duration = column[Option[Long]]("DURATION")

    def scoreLimit = column[Option[Double]]("SCORE_LIMIT")

    def playerTitle = column[String]("PLAYER_TITLE")

    def topDownNavigation = column[Boolean]("TOP_DOWN_NAVIGATION")

    def activityId = column[String]("ACTIVITY_ID")

    def status = column[String]("STATUS")

    def version = column[Double]("VERSION")

    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def oneAnswerAttempt = column[Boolean]("ONE_ANSWER_ATTEMPT")

    def lockUserId = column[Option[Long]]("LOCK_USER_ID")

    def lockDate = column[Option[DateTime]]("LOCK_DATE")

    def requiredReview = column[Boolean]("REQUIRED_REVIEW")

    def * = (
      id,
      title,
      description,
      courseId,
      logo,
      isTemplate,
      isSelectedContinuity,
      themeId,
      duration,
      scoreLimit,
      playerTitle,
      topDownNavigation,
      activityId,
      status,
      version,
      modifiedDate,
      oneAnswerAttempt,
      lockUserId,
      lockDate,
      requiredReview) <>(SlideSet.tupled, SlideSet.unapply)
  }

  case class Slide(id: Long,
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
                   isLessonSummary: Boolean = false,
                   playerTitle: Option[String] = None)

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

    def playerTitle = column[Option[String]]("PLAYER_TITLE")

    def * = (
      id,
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
      playerTitle) <>(Slide.tupled, Slide.unapply)
  }

  case class SlideElement(id: Long,
                          zIndex: String,
                          content: String,
                          slideEntityType: String,
                          slideId: Long,
                          correctLinkedSlideId: Option[Long] = None,
                          incorrectLinkedSlideId: Option[Long] = None,
                          notifyCorrectAnswer: Option[Boolean] = None)

  class SlideElementTable(tag: Tag) extends Table[SlideElement](tag, tblName("SLIDE_ELEMENT")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def zIndex = column[String]("Z_INDEX")

    def content = column[String]("CONTENT", O.DBType(varCharMax))

    def slideEntityType = column[String]("SLIDE_ENTITY_TYPE")

    def slideId = column[Long]("SLIDE_ID")

    def correctLinkedSlideId = column[Option[Long]]("CORRECT_LINKED_SLIDE_ID")

    def incorrectLinkedSlideId = column[Option[Long]]("INCORRECT_LINKED_SLIDE_ID")

    def notifyCorrectAnswer = column[Option[Boolean]]("NOTIFY_CORRECT_ANSWER")

    def * = (
      id,
      zIndex,
      content,
      slideEntityType,
      slideId,
      correctLinkedSlideId,
      incorrectLinkedSlideId,
      notifyCorrectAnswer) <>(SlideElement.tupled, SlideElement.unapply)
  }

  val slideSets = TableQuery[SlideSetTable]
  val slides = TableQuery[SlideTable]
  val slideElements = TableQuery[SlideElementTable]
}
