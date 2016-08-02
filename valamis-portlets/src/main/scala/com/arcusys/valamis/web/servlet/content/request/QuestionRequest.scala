package com.arcusys.valamis.web.servlet.content.request

import com.arcusys.valamis.content.model.{AnswerKeyValue, AnswerRange, AnswerText}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.content.response.{AnswerResponse, AnswerSerializer}
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequestModel, BaseRequest, Parameter}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraBase

object QuestionRequest extends BaseRequest {

  val NewCourseId = "newCourseID"
  val CategoryId = "categoryID"
  val CategoryIds = "categoryIDs"

  val Id = "id"
  val QuestionType = "questionType"
  val Title = "title"
  val Text = "text"
  val ExplanationText = "explanationText"
  val RightAnswerText = "rightAnswerText"
  val WrongAnswerText = "wrongAnswerText"
  val Force = "forceCorrectCount"
  val Case = "isCaseSensitive"
  val Answers = "answers"
  val Index = "index"
  val ParentId = "parentID"
  val QuestionIds = "questionIDs"
  val ContentIds = "contentIDs"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {
    implicit val httpRequest = scalatra.request
    implicit val fs: Formats = DefaultFormats + new AnswerSerializer

    def action = QuestionActionType.withName(Parameter(Action).required.toUpperCase)

    def id = Parameter(Id).longRequired

    def idOption = Parameter(Id).longOption

    def courseId = Parameter(CourseId).longRequired

    @deprecated
    def courseIdOption = Parameter(CourseId).intOption

    def newCourseId = Parameter(NewCourseId).intOption

    def questionType = Parameter(QuestionType).intRequired

    def categoryId = Parameter(CategoryId).longOption

    def title = Parameter(Title).required

    def text = Parameter(Text).withDefault("")

    def explanationText = Parameter(ExplanationText).withDefault("")

    def rightAnswerText = Parameter(RightAnswerText).withDefault("")

    def wrongAnswerText = Parameter(WrongAnswerText).withDefault("")

    def forceCorrectCount = Parameter(Force).booleanRequired

    def isCaseSensitive = Parameter(Case).booleanRequired

    def answers = Parameter(Answers).withDefault("[]")

    def equalsAnswers : Seq[AnswerText] = {
      JsonHelper.fromJson[List[AnswerResponse]](answers)
        .map(a => AnswerText(None, None, courseId, a.answerText, a.isCorrect, 0,a.score))
    }

    def rangeAnswers : Seq[AnswerRange] = {
      JsonHelper.fromJson[List[AnswerResponse]](answers)
        .map(a => AnswerRange(None, None, courseId, a.rangeFrom.toDouble, a.rangeTo.toDouble, a.score))
    }

    def keyValueAnswers : Seq[AnswerKeyValue] = {
      JsonHelper.fromJson[List[AnswerResponse]](answers)
        .map(a => AnswerKeyValue(None, None, courseId, a.answerText, Some(a.matchingText), a.score))
    }

    def parentId = Parameter(ParentId).longOption

    def index = Parameter(Index).intRequired

    def questionIds = Parameter(QuestionIds).multiLong

    def contentIds = Parameter(ContentIds).multiLong
  }

}
