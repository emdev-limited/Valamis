package com.arcusys.learn.liferay.update.version310.slides3101

import com.arcusys.valamis.tag.model.ValamisTag
import org.joda.time.DateTime

object SlideSetStatus {
  val Published = "published"
  val Draft = "draft"
  val Archived = "archived"
}

case class SlideSet(id: Long = 0L,
                    title: String = "",
                    description: String = "",
                    courseId: Long,
                    logo: Option[String] = None,
                    slides: Seq[Slide] = Seq(),
                    isTemplate: Boolean = false,
                    isSelectedContinuity: Boolean = false,
                    themeId: Option[Long] = None,
                    duration: Option[Long] = None,
                    scoreLimit: Option[Double] = None,
                    playerTitle: String = "page",
                    slidesCount: Option[Long] = None,
                    topDownNavigation: Boolean = false,
                    activityId: String = "",
                    status: String = SlideSetStatus.Draft,
                    version: Double = 1.0,
                    modifiedDate: DateTime = new DateTime(),
                    oneAnswerAttempt: Boolean = false,
                    tags: Seq[ValamisTag] = Seq(),
                    lockUserId: Option[Long] = None,
                    lockDate: Option[DateTime] = None,
                    requiredReview: Boolean = false,
                    lockUserName: Option[String] = None)

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


case class SlideElement(id: Long = 0L,
                        zIndex: String = "1",
                        content: String = "",
                        slideEntityType: String,
                        slideId: Long,
                        correctLinkedSlideId: Option[Long] = None,
                        incorrectLinkedSlideId: Option[Long] = None,
                        notifyCorrectAnswer: Option[Boolean] = None,
                        properties: Seq[Properties] = Seq())


case class Properties(deviceId: Long, properties: Seq[Property])

case class Property(key: String, value: String)