package com.arcusys.learn.liferay.update.version260.model

import com.arcusys.valamis.persistence.common.DbNameUtils._

import scala.slick.driver.JdbcProfile

/**
  * Created by Igor Borisov on 02.11.15.
  */
trait SlideElementPropertyTableComponent {
  protected val driver: JdbcProfile

  import driver.simple._

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
  }

    case class Device(id: Option[Long] = None,
                      name: String,
                      minWidth: Int,
                      maxWidth: Int,
                      minHeight: Int,
                      margin: Int)

    class DeviceTable(tag: Tag) extends Table[Device](tag, tblName("DEVICE")) {
      def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
      def name = column[String]("NAME", O.Length(20, varying = true))
      def minWidth = column[Int]("MIN_WIDTH")
      def maxWidth = column[Int]("MAX_WIDTH")
      def minHeight = column[Int]("MIN_HEIGHT")
      def margin = column[Int]("MARGIN")

      def * = (id.?, name, minWidth, maxWidth, minHeight, margin) <>(Device.tupled, Device.unapply)
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
    }


    case class SlideElementProperty(slideElementId: Long, deviceId: Long, key: String, value: String)

    class SlideElementPropertyTable(tag: Tag) extends Table[SlideElementProperty](tag, tblName("SLIDE_ELEMENT_PROPERTY")) {
      def slideElementId = column[Long]("SLIDE_ELEMENT_ID")
      def deviceId = column[Long]("DEVICE_ID")
      def key = column[String]("DATA_KEY", O.Length(254, true))
      def value = column[String]("DATA_VALUE", O.Length(254, true))
      def pk = primaryKey("PK_PROPERTY", (slideElementId, deviceId, key))

      def * = (slideElementId, deviceId, key, value) <>(SlideElementProperty.tupled, SlideElementProperty.unapply)

      def slideElementFK = foreignKey("PROPERTY_ELEMENT", slideElementId, slideElements)(_.id, onDelete = ForeignKeyAction.Cascade)
      def deviceFK = foreignKey("PROPERTY_DEVICE", deviceId, devices)(_.id, onDelete = ForeignKeyAction.Cascade)
    }

    case class SlideProperty(slideId: Long, deviceId: Long, key: String, value: String)

    class SlidePropertyTable(tag: Tag) extends Table[SlideProperty](tag, tblName("SLIDE_PROPERTY")) {
      def slideId = column[Long]("SLIDE_ID")
      def deviceId = column[Long]("DEVICE_ID")
      def key = column[String]("DATA_KEY", O.Length(254, true))
      def value = column[String]("DATA_VALUE")
      def pk = primaryKey("PK_SLIDE_PROPERTY", (slideId, deviceId, key))

      def * = (slideId, deviceId, key, value) <>(SlideProperty.tupled, SlideProperty.unapply)

      def slideSetFK = foreignKey(fkName("PROPERTY_TO_SLIDE"), slideId, slides)(_.id, onDelete = ForeignKeyAction.Cascade)
      def deviceFK = foreignKey(fkName("PROPERTY_TO_DEVICE"), deviceId, devices)(_.id, onDelete = ForeignKeyAction.Cascade)
    }

    val slides = TableQuery[SlideTable]
    val devices = TableQuery[DeviceTable]
    val slideElements = TableQuery[SlideElementTable]
    val slideElementProperties = TableQuery[SlideElementPropertyTable]
    val slideProperties = TableQuery[SlidePropertyTable]
  }
