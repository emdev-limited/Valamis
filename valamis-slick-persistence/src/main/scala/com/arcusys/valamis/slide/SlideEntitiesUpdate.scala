package com.arcusys.valamis.slide

case class SlideSetEntityUpdate(
    title: String = "",
    description: String = "",
    courseId: Long,
    logo: Option[String] = None,
    isTemplate: Boolean = false,
    isSelectedContinuity: Boolean = false,
    themeId: Option[Long] = None,
    duration: Option[Long] = None,
    scoreLimit: Option[Double] = None,
    playerTitle: String = "page")

case class SlideEntityUpdate(
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

case class SlideElementEntityUpdate(
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
