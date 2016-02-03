package com.arcusys.learn.liferay.update.version250.model

// model from Learn_LFSlideSet table
case class LFSlideSet(
                           id: Long,
                           title: Option[String],
                           description: Option[String],
                           logo: Option[String],
                           courseId: Option[Long])

// model from Learn_LFSlide table
case class LFSlide(
                    id: Long,
                    bgColor: Option[String],
                    bgImage: Option[String],
                    questionFont: Option[String],
                    answerFont: Option[String],
                    answerBg: Option[String],
                    font: Option[String],
                    title: Option[String],
                    duration: Option[String],
                    slideSetId: Option[Long],
                    topSlideId: Option[Long],
                    leftSlideId: Option[Long],
                    statementVerb: Option[String],
                    statementObject: Option[String],
                    statementCategoryId: Option[String])

// model from Learn_LFSlideEntity table
case class LFSlideEntity(id: Long,
                         top: Option[String],
                         left: Option[String],
                         width: Option[String],
                         height: Option[String],
                         zIndex: Option[String],
                         content: Option[String],
                         slideEntityType: Option[String],
                         slideId: Option[Long],
                         correctLinkedSlideId: Option[Long],
                         incorrectLinkedSlideId: Option[Long],
                         notifyCorrectAnswer: Option[Boolean])

