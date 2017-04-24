package com.arcusys.valamis.persistence.impl.slide.schema

import com.arcusys.valamis.slide.model.{Slide, SlideElement, SlideSet}
import org.joda.time.DateTime

trait SlideTableMapping {

  type SlideSetData = (
    Long,
      String,
      String,
      Long,
      Option[String],
      Boolean,
      Boolean,
      Option[Long],
      Option[Long],
      Option[Double],
      String,
      Boolean,
      String,
      String,
      Double,
      DateTime,
      Boolean,
      Option[Long],
      Option[DateTime],
      Boolean)

  type SlideData = (
    Long,
      String,
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[Long],
      Option[Long],
      Long,
      Option[String],
      Option[String],
      Option[String],
      Boolean,
      Boolean,
      Option[String])

  type ElementData = (Long,
    String,
    String,
    String,
    Long,
    Option[Long],
    Option[Long],
    Option[Boolean])

  def constructSlideSet: SlideSetData => SlideSet = {
    case (
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
      requiredReview) =>
      SlideSet(
        id = id,
        title = title,
        description = description,
        courseId = courseId,
        logo = logo,
        isTemplate = isTemplate,
        isSelectedContinuity = isSelectedContinuity,
        themeId = themeId,
        duration = duration,
        scoreLimit = scoreLimit,
        playerTitle = playerTitle,
        topDownNavigation = topDownNavigation,
        activityId = activityId,
        status = status,
        version = version,
        modifiedDate = modifiedDate,
        oneAnswerAttempt = oneAnswerAttempt,
        lockUserId = lockUserId,
        lockDate = lockDate,
        requiredReview = requiredReview)
  }

  def constructSlide: SlideData => Slide = {
    case (
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
      playerTitle) =>
      Slide(
        id = id,
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
        slideSetId = slideSetId,
        statementVerb = statementVerb,
        statementObject = statementObject,
        statementCategoryId = statementCategoryId,
        isTemplate = isTemplate,
        isLessonSummary = isLessonSummary,
        playerTitle = playerTitle)
  }

  def extractSlide: PartialFunction[Slide, SlideData] = {
    case Slide(
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
    _,
    slideSetId,
    statementVerb,
    statementObject,
    statementCategoryId,
    isTemplate,
    isLessonSummary,
    playerTitle,
    _) =>
      (id,
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
        playerTitle)
  }

  def constructSlideElement: ElementData => SlideElement = {
    case (
      id,
      zIndex,
      content,
      slideEntityType,
      slideId,
      correctLinkedSlideId,
      incorrectLinkedSlideId,
      notifyCorrectAnswer) =>
      SlideElement(
        id = id,
        zIndex = zIndex,
        content = content,
        slideEntityType = slideEntityType,
        slideId = slideId,
        correctLinkedSlideId = correctLinkedSlideId,
        incorrectLinkedSlideId = incorrectLinkedSlideId,
        notifyCorrectAnswer = notifyCorrectAnswer)
  }

  def extractSlideElement: PartialFunction[SlideElement, ElementData] = {
    case SlideElement(
    id,
    zIndex,
    content,
    slideEntityType,
    slideId,
    correctLinkedSlideId,
    incorrectLinkedSlideId,
    notifyCorrectAnswer,
    _) =>
      (id,
        zIndex,
        content,
        slideEntityType,
        slideId,
        correctLinkedSlideId,
        incorrectLinkedSlideId,
        notifyCorrectAnswer)
  }
}
