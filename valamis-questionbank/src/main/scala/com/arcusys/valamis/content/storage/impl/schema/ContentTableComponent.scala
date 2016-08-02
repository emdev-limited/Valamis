package com.arcusys.valamis.content.storage.impl.schema

import com.arcusys.valamis.content.model.AnswerType.AnswerType
import com.arcusys.valamis.content.model.QuestionType.QuestionType
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent3, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait ContentTableComponent extends LongKeyTableComponent3 with TypeMapper { self: SlickProfile =>

  import driver.api._

  protected def makeCustomAnswer(a: AnswerRow): Answer = a.answerType match {
    case AnswerType.Text => AnswerText(a.id, a.questionId, a.courseId, a.text, a.isCorrect, a.position, a.score)
    case AnswerType.Range => AnswerRange(a.id, a.questionId, a.courseId, a.rangeFrom, a.rangeTo, a.score)
    case AnswerType.KeyValue => AnswerKeyValue(a.id, a.questionId, a.courseId, a.text, a.value, a.score)
  }

  protected def makeAnswerRow(questionId: Long, a: Answer): AnswerRow = a match {
    case aa: AnswerText => AnswerRow(a.id, Some(questionId), a.courseId, aa.body, aa.isCorrect, aa.position, 0, 0, None, a.score, AnswerType.Text)
    case aa: AnswerRange => AnswerRow(a.id, Some(questionId), a.courseId, "", isCorrect = false, 0, aa.rangeFrom, aa.rangeTo, None, a.score, AnswerType.Range)
    case aa: AnswerKeyValue => AnswerRow(a.id, Some(questionId), a.courseId, aa.key, isCorrect = false, 0, 0, 0, aa.value, a.score, AnswerType.KeyValue)
  }

  class CategoryTable(tag: Tag) extends LongKeyTable3[Category](tag, "CONTENT_CATEGORIES") {

    def title = column[String]("TITLE",O.Length(512,varying=true))

    def description = column[String]("DESCRIPTION",O.DBType(varCharMax))

    def parentId = column[Option[Long]]("PARENT_ID")

    def courseId = column[Long]("COURSE_ID")

    def * = (id.?, title, description, parentId, courseId) <> (Category.tupled, Category.unapply)

    def update = (title, description, parentId, courseId) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def parent = foreignKey(fkName("CATEGORY_TO_PARENT"), parentId, questionCategories)(_.id, onDelete = ForeignKeyAction.NoAction)

  }


  class PlainTextTable(tag: Tag) extends LongKeyTable3[PlainText](tag, "CONTENT_PLAIN_TEXTS") {

    def categoryId = column[Option[Long]]("CATEGORY_ID")

    def title = column[String]("TITLE",O.Length(512,varying=true))

    def text = column[String]("TEXT",O.SqlType(varCharMax))

    def courseId = column[Long]("COURSE_ID")

    def * = (id.?, categoryId, title, text, courseId) <> (PlainText.tupled, PlainText.unapply)

    def update = (categoryId, title, text, courseId) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def category = foreignKey(fkName("PLAIN_TEXT_CATEGORIES"), categoryId, questionCategories)(_.id, onDelete = ForeignKeyAction.Cascade)

  }

  case class BaseQuestion(id: Option[Long],
                          categoryId: Option[Long],
                          courseId: Long,
                          title: String,
                          text: String,
                          explanationText: String,
                          rightAnswerText: String,
                          wrongAnswerText: String,
                          forceCorrectCount: Boolean,
                          isCaseSensitive: Boolean,
                          questionType: QuestionType,
                          parentQuestionId: Option[Long] = None)

  class QuestionTable(tag: Tag) extends LongKeyTable3[BaseQuestion](tag, "CONTENT_QUESTIONS") {

    implicit val QuestionTypeMapper = MappedColumnType.base[QuestionType, String](
      s => s.toString,
      s => QuestionType.withName(s)
    )

    def categoryId = column[Option[Long]]("CATEGORY_ID")

    def courseId = column[Long]("COURSE_ID")

    def title = column[String]("TITLE",O.Length(512,varying=true))

    def text = column[String]("TEXT",O.DBType(varCharMax))

    def explanationText = column[String]("EXPLANATION_TEXT",O.DBType(varCharMax))

    def rightAnswerText = column[String]("RIGHT_ANSWER_TEXT",O.DBType(varCharMax))

    def wrongAnswerText = column[String]("WRONG_ANSWER_TEXT",O.DBType(varCharMax))

    def forceCorrectCount = column[Boolean]("FORCE_CORRECT_COUNT")

    def isCaseSensitive = column[Boolean]("IS_CASE_SENSITIVE")

    def questionType = column[QuestionType]("QUESTION_TYPE")

    def parentQuestionId = column[Option[Long]]("PARENT_QUESTION_ID")

    def * = (id.?,
      categoryId,
      courseId,
      title,
      text,
      explanationText,
      rightAnswerText,
      wrongAnswerText,
      forceCorrectCount,
      isCaseSensitive,
      questionType,
      parentQuestionId) <>(BaseQuestion.tupled, BaseQuestion.unapply)

    def update = (
      categoryId,
      courseId,
      title,
      text,
      explanationText,
      rightAnswerText,
      wrongAnswerText,
      forceCorrectCount,
      isCaseSensitive,
      questionType,
      parentQuestionId) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def category = foreignKey(fkName("QUESTIONS_CATEGORIES"), categoryId, questionCategories)(_.id, onDelete = ForeignKeyAction.Cascade)

    def parentQuestion = foreignKey(fkName("QUESTIONS_QUESTIONS"), parentQuestionId, questions)(_.id, onDelete = ForeignKeyAction.NoAction)

  }

  case class AnswerRow(id: Option[Long],
                       questionId: Option[Long],
                       courseId: Long,
                       text: String, //key
                       isCorrect: Boolean,
                       position: Int,
                       rangeFrom: Double,
                       rangeTo: Double,
                       value: Option[String],
                       score: Option[Double],
                       answerType: AnswerType)


  class AnswerTable(tag: Tag) extends LongKeyTable3[AnswerRow](tag, "CONTENT_ANSWERS") {

    def questionId = column[Option[Long]]("QUESTION_ID")

    def courseId = column[Long]("COURSE_ID")

    def text = column[String]("TEXT",O.DBType(varCharMax)) //key

    def isCorrect = column[Boolean]("IS_CORRECT")

    def position = column[Int]("POSITION")

    def rangeFrom = column[Double]("RANGE_FROM")

    def rangeTo = column[Double]("RANGE_TO")

    def value = column[Option[String]]("ITEM",O.DBType(varCharMax))

    def score = column[Option[Double]]("SCORE")

    def answerType = column[AnswerType]("ANSWER_TYPE")

    implicit val AnswerTypeMapper = MappedColumnType.base[AnswerType, String](
      s => s.toString,
      s => AnswerType.withName(s)
    )

    def * = (id.?, questionId, courseId, text, isCorrect, position, rangeFrom, rangeTo, value, score, answerType) <>(AnswerRow.tupled, AnswerRow.unapply)

    def update = (questionId, courseId, text, isCorrect, position, rangeFrom, rangeTo, value, score, answerType) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def question = foreignKey(fkName("ANSWERS_QUESTIONS"), questionId, questions)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  val questionCategories = TableQuery[CategoryTable]
  val questions = TableQuery[QuestionTable]
  val plainTexts = TableQuery[PlainTextTable]

  val answers = TableQuery[AnswerTable]


}
