package com.arcusys.valamis.content.export

import java.io.File

import com.arcusys.valamis.content.export.model._
import com.arcusys.valamis.content.model.QuestionType.QuestionType
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.service.{CategoryService, PlainTextService, QuestionService}
import com.arcusys.valamis.util.export.ImportProcessor
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class QuestionImportProcessor(implicit val bindingModule: BindingModule)
  extends ImportProcessor[QuestionCategoryExport]
  with Injectable {


  private lazy val questionService = inject[QuestionService]
  private lazy val plainTextService = inject[PlainTextService]
  private lazy val catService = inject[CategoryService]

  override def importItems(items: List[QuestionCategoryExport],
                           courseId: Long,
                           tempDirectory: File,
                           userId: Long,
                           data: String): Unit = {
    items.foreach(qc => {
      val catId = importCategory(qc,None,courseId)
      qc.childrenCats.foreach(importCategory(_,catId,courseId))
    })
  }

  def importCategory(qc: QuestionCategoryExport, parentId:Option[Long],courseId: Long): Option[Long] = {
    val categoryId = if (qc.title == "root") {
      None
    } else {
      catService.create(Category(None, qc.title, qc.description, parentId, courseId)).id
    }
    qc.children.foreach {
      case q: QuestionExport if q.questionType != 8 =>
        val (question, answers) = fromExportFormat(categoryId, courseId, q)
        questionService.create(question, answers)
      case p: QuestionExport if p.questionType == 8 =>
        plainTextService.create(PlainText(None, categoryId, p.title, p.text, courseId))
    }

    categoryId
  }

  private def fromExportFormat(questionType: QuestionType, courseId: Long, input: AnswerExport): Answer = questionType match {
    case QuestionType.Choice | QuestionType.Text | QuestionType.Positioning =>
      new AnswerText(None, None, courseId, input.answerText, input.isCorrect,0,input.score)
    case QuestionType.Numeric =>
      new AnswerRange(None, None, courseId, input.rangeFrom.toDouble, input.rangeTo.toDouble, input.score)
    case QuestionType.Matching | QuestionType.Categorization =>
      new AnswerKeyValue(None, None, courseId, input.answerText, Some(input.matchingText), input.score)
  }

  private def fromExportFormat(categoryID: Option[Long], courseId: Long, input: QuestionExport): (Question, Seq[Answer]) = {
    val qType = Question.getTypeByCode(input.questionType)
    qType match {
      case QuestionType.Choice => (new ChoiceQuestion(
        None,
        categoryID,
        input.title,
        input.text,
        input.explanationText,
        input.rightAnswerText.getOrElse(""),
        input.wrongAnswerText.getOrElse(""),
        input.forceCorrectCount,
        courseId
      ), input.answers.map(fromExportFormat(qType, courseId, _)))
      case QuestionType.Text => (new TextQuestion(
        None,
        categoryID,
        input.title,
        input.text,
        input.explanationText,
        input.rightAnswerText.getOrElse(""),
        input.wrongAnswerText.getOrElse(""),
        input.isCaseSensitive,
        courseId
      ), input.answers.map(fromExportFormat(qType, courseId, _)))
      case QuestionType.Numeric => (new NumericQuestion(
        None,
        categoryID,
        input.title,
        input.text,
        input.explanationText,
        input.rightAnswerText.getOrElse(""),
        input.wrongAnswerText.getOrElse(""),
        courseId
      ), input.answers.map(fromExportFormat(qType, courseId, _)))
      case QuestionType.Positioning => (new PositioningQuestion(
        None,
        categoryID,
        input.title,
        input.text,
        input.explanationText,
        input.rightAnswerText.getOrElse(""),
        input.wrongAnswerText.getOrElse(""),
        input.forceCorrectCount,
        courseId
      ), input.answers.map(fromExportFormat(qType, courseId, _)))
      case QuestionType.Matching => (new MatchingQuestion(
        None,
        categoryID,
        input.title,
        input.text,
        input.explanationText,
        input.rightAnswerText.getOrElse(""),
        input.wrongAnswerText.getOrElse(""),
        courseId
      ), input.answers.map(fromExportFormat(qType, courseId, _)))
      case QuestionType.Essay => (new EssayQuestion(
        None,
        categoryID,
        input.title,
        input.text,
        input.explanationText,
        courseId
      ), Seq())
      case QuestionType.Categorization => (new CategorizationQuestion(
        None,
        categoryID,
        input.title,
        input.text,
        input.explanationText,
        input.rightAnswerText.getOrElse(""),
        input.wrongAnswerText.getOrElse(""),
        courseId
      ), input.answers.map(fromExportFormat(qType, courseId, _)))
    }


  }
}

