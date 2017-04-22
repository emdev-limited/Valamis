package com.arcusys.valamis.web.servlet.slides.response

import com.arcusys.valamis.slide.model.{SlideElement,Slide}

case class SlideElementResponse(id: Long,
                                zIndex: String,
                                content: String,
                                slideEntityType: String,
                                slideId: Long,
                                correctLinkedSlideId: Option[Long],
                                incorrectLinkedSlideId: Option[Long],
                                notifyCorrectAnswer: Option[Boolean],
                                properties: Map[Long, Map[String, String]])

case class SlideResponse(id: Long,
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
                         slideElements: Seq[SlideElementResponse],
                         slideSetId: Long,
                         statementVerb: Option[String],
                         statementObject: Option[String],
                         statementCategoryId: Option[String],
                         isTemplate: Boolean,
                         isLessonSummary: Boolean,
                         playerTitle: Option[String] = None,
                         properties: Map[Long, Map[String, String]])


object SlideConverter {
  implicit class SlideModelExt(val model: Slide) extends AnyRef {
    import SlideElementConverter._

    def convertSlideModel: SlideResponse = {
      val newSlideElements = model.slideElements.map(_.convertSlideElementModel)
      val newProperties = model.properties
        .map(x => (x.deviceId,
          x.properties.map(p => (p.key, p.value)).toMap)).toMap
      SlideResponse(
        model.id,
        model.title,
        model.bgColor,
        model.bgImage,
        model.font,
        model.questionFont,
        model.answerFont,
        model.answerBg,
        model.duration,
        model.leftSlideId,
        model.topSlideId,
        newSlideElements,
        model.slideSetId,
        model.statementVerb,
        model.statementObject,
        model.statementCategoryId,
        model.isTemplate,
        model.isLessonSummary,
        model.playerTitle,
        newProperties)
    }
  }
}

object SlideElementConverter {
  implicit class SlideElementModelExt(val model: SlideElement) extends AnyRef {

    def convertSlideElementModel : SlideElementResponse = {
      val newProperties = model.properties
        .map(x => (x.deviceId,
          x.properties.map(p => (p.key, p.value)).toMap)).toMap

      SlideElementResponse(
        model.id,
        model.zIndex,
        model.content,
        model.slideEntityType,
        model.slideId,
        model.correctLinkedSlideId,
        model.incorrectLinkedSlideId,
        model.notifyCorrectAnswer,
        newProperties
      )
    }
  }
}
