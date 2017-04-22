package com.arcusys.valamis.slide.model

import com.arcusys.valamis.tag.model.ValamisTag
import org.joda.time.DateTime

object SlideSetStatus {
  val Published = "published"
  val Draft = "draft"
  val Archived = "archived"
}

object SlideEntityType {
  val Text = "text"
  val Iframe = "iframe"
  val Question = "question"
  val Image = "image"
  val Video = "video"
  val Pdf = "pdf"
  val Math = "math"
  val Webgl = "webgl"
  val PlainText = "plaintext"
  val RandomQuestion = "randomquestion"
  val Audio = "audio"
  val AvailableTypes = Text :: Iframe :: Question :: Image :: Video :: Pdf :: Math :: Webgl :: PlainText :: RandomQuestion :: Audio :: Nil
  val AvailableExternalFileTypes = Image :: Pdf :: Webgl :: Audio :: Nil
}

case class SlideSet(id: Long = 0L,
                    title: String = "",
                    description: String = "",
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
                    status: String = SlideSetStatus.Draft,
                    version: Double = 1.0,
                    modifiedDate: DateTime = new DateTime(),
                    oneAnswerAttempt: Boolean = false,
                    lockUserId: Option[Long] = None,
                    lockDate: Option[DateTime] = None,
                    requiredReview: Boolean = false)

case class Slide(id: Long = 0L,
                 title: String = "Page",
                 bgColor: Option[String] = None,
                 bgImage: Option[String] = None,
                 font: Option[String] = None, //3 parameters separated with $ (family, size, color)
                 questionFont: Option[String] = None,
                 answerFont: Option[String] = None,
                 answerBg: Option[String] = None,
                 duration: Option[String] = None,
                 leftSlideId: Option[Long] = None,
                 topSlideId: Option[Long] = None,
                 slideElements: Seq[SlideElement] = Seq(),
                 slideSetId: Long,
                 statementVerb: Option[String] = None,
                 statementObject: Option[String] = None,
                 statementCategoryId: Option[String] = None,
                 isTemplate: Boolean = false,
                 isLessonSummary: Boolean = false,
                 playerTitle: Option[String] = None,
                 properties: Seq[Properties] = Seq())

// We don't construct Slide*Model into hierarchy, because all rendering is done on client-side
// We add extra field slideEntityType, because our serialization erases type information
case class SlideElement(id: Long = 0L,
                        zIndex: String = "1",
                        content: String = "",
                        slideEntityType: String,
                        slideId: Long,
                        correctLinkedSlideId: Option[Long] = None,
                        incorrectLinkedSlideId: Option[Long] = None,
                        notifyCorrectAnswer: Option[Boolean] = None,
                        properties: Seq[Properties] = Seq())

case class SlideElementPropertyEntity(slideElementId: Long, deviceId: Long, key: String, value: String) extends PropertyEntity

case class SlidePropertyEntity(slideId: Long, deviceId: Long, key: String, value: String) extends PropertyEntity

trait PropertyEntity {
  def deviceId: Long
  def key: String
  def value: String
}

case class Property(key: String, value: String)

case class Properties(deviceId: Long, properties: Seq[Property])

case class Device(id: Long = 0L,
                  name: String,
                  minWidth: Int,
                  maxWidth: Int,
                  minHeight: Int,
                  margin: Int)

case class SlideTheme(id: Long = 0L,
                      title: String = "Theme",
                      bgColor: Option[String] = None,
                      bgImage: Option[String] = None, //2 parameters separated with $ (image and its size(mode))
                      font: Option[String] = None, //3 parameters separated with $ (family, size, color)
                      questionFont: Option[String] = None,
                      answerFont: Option[String] = None,
                      answerBg: Option[String] = None,
                      userId: Option[Long] = None,
                      isDefault: Boolean = false)

case class SlideOldElementModel(id: Long,
                                top: String = "0",
                                left: String = "0",
                                width: String = "800",
                                height: String = "auto")
