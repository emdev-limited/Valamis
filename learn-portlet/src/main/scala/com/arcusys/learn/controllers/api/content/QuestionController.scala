package com.arcusys.learn.controllers.api.content

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.models.ContentResponseBuilder
import com.arcusys.learn.models.request.QuestionRequest
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.service.QuestionService

import scala.util.Try

class QuestionController extends BaseApiController with ContentPolicy {

  lazy val questionService = inject[QuestionService]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/questions(/)(:id)")(jsonAction {
    val questionRequest = QuestionRequest(this)
    ContentResponseBuilder.toResponse(questionService.getQuestionNodeById(questionRequest.id))
  })

  //create question
  post("/questions/add/:type(/)")(jsonAction {
    val questionType = Try(QuestionType.withName(params("type"))).getOrElse(pass())
    val (question, answers) = extractQuestionFromRequest(questionType)
    val newQuestion = questionService.create(question, answers)
    ContentResponseBuilder.toResponse(newQuestion, questionService.getAnswers(newQuestion.id.get))
  })

  //update question
  post("/questions/update/:id(/)")(jsonAction {
    val req = QuestionRequest(this)
    val questionType = Question.getTypeByCode(req.questionType)
    val (question, answers) = extractQuestionFromRequest(questionType)
    questionService.update(question, answers)
  })

  //delete question
  post("/questions/delete/:id(/)")(jsonAction {
    val req = QuestionRequest(this)
    questionService.delete(req.id)
  })

  //move question
  post("/questions/move/:id(/)")(jsonAction {
    val req = QuestionRequest(this)
    questionService.moveToCategory(req.id, req.parentId, req.courseId)
  })

  post("/questions/moveToCourse(/)")(jsonAction {
    val req = QuestionRequest(this)
    req.questionIds
      .foreach(id => questionService.moveToCourse(id, req.newCourseId.get, moveToRoot = true))
  })

  private def extractQuestionFromRequest(questionType: QuestionType.Value): (Question, Seq[Answer]) = {
    val req = QuestionRequest(this)
    questionType match {
      case QuestionType.Choice => (new ChoiceQuestion(
        req.idOption,
        req.categoryId,
        req.title,
        req.text,
        req.explanationText,
        req.rightAnswerText,
        req.wrongAnswerText,
        req.forceCorrectCount,
        req.courseId
      ), req.equalsAnswers)
      case QuestionType.Text => (new TextQuestion(
        req.idOption,
        req.categoryId,
        req.title,
        req.text,
        req.explanationText,
        req.rightAnswerText,
        req.wrongAnswerText,
        req.isCaseSensitive,
        req.courseId
      ), req.equalsAnswers)
      case QuestionType.Numeric => (new NumericQuestion(
        req.idOption,
        req.categoryId,
        req.title,
        req.text,
        req.explanationText,
        req.rightAnswerText,
        req.wrongAnswerText,
        req.courseId
      ), req.rangeAnswers)
      case QuestionType.Positioning => (new PositioningQuestion(
        req.idOption,
        req.categoryId,
        req.title,
        req.text,
        req.explanationText,
        req.rightAnswerText,
        req.wrongAnswerText,
        req.forceCorrectCount,
        req.courseId
      ), req.equalsAnswers)
      case QuestionType.Matching => (new MatchingQuestion(
        req.idOption,
        req.categoryId,
        req.title,
        req.text,
        req.explanationText,
        req.rightAnswerText,
        req.wrongAnswerText,
        req.courseId
      ), req.keyValueAnswers)
      case QuestionType.Essay => (new EssayQuestion(
        req.idOption,
        req.categoryId,
        req.title,
        req.text,
        req.explanationText,
        req.courseId
      ), Seq())
      case QuestionType.Categorization => (new CategorizationQuestion(
        req.idOption,
        req.categoryId,
        req.title,
        req.text,
        req.explanationText,
        req.rightAnswerText,
        req.wrongAnswerText,
        req.courseId
      ), req.keyValueAnswers)
    }
  }

}
