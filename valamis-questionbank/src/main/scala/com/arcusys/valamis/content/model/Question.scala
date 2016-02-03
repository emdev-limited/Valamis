package com.arcusys.valamis.content.model

import com.arcusys.valamis.content.model.QuestionType.QuestionType

//TODO: remove id using
object QuestionType extends Enumeration {
  type QuestionType = Value
  val Choice = Value(0, "choice")
  val Text = Value(1, "text")
  val Numeric = Value(2, "numeric")
  val Positioning = Value(3, "positioning")
  val Matching = Value(4, "matching")
  val Essay = Value(5, "essay")
  val Categorization = Value(7, "categorization")
}

//TODO delete after got rid of index for question types
object Question {
  def getTypeByCode(typeCode: Int): QuestionType.Value = {
    typeCode match {
      case 0 => QuestionType.Choice
      case 1 => QuestionType.Text
      case 2 => QuestionType.Numeric
      case 3 => QuestionType.Positioning
      case 4 => QuestionType.Matching
      case 5 => QuestionType.Essay
      case 7 => QuestionType.Categorization
    }
  }
}

sealed trait Question extends Content {
  def id: Option[Long]

  def categoryId: Option[Long]

  def courseId: Long

  def title: String

  def text: String

  def explanationText: String

  def questionType: QuestionType

  def contentType = ContentType.Question
}

/**
Related data:
  answers (AnswerText)
  */
case class ChoiceQuestion(id: Option[Long],
                          categoryId: Option[Long],
                          title: String,
                          text: String,
                          explanationText: String,
                          rightAnswerText: String,
                          wrongAnswerText: String,
                          forceCorrectCount: Boolean,
                          courseId: Long,
                          parentQuestionId: Option[Long] = None)
  extends Question {
  override def questionType = QuestionType.Choice
}

/**
 * Short answer question
Related data:
  answers (AnswerText)
 */
case class TextQuestion(id: Option[Long],
                        categoryId: Option[Long],
                        title: String,
                        text: String,
                        explanationText: String,
                        rightAnswerText: String,
                        wrongAnswerText: String,
                        isCaseSensitive: Boolean,
                        courseId: Long,
                        parentQuestionId: Option[Long] = None)
  extends Question {
  def questionType = QuestionType.Text
}


/**
Related data:
  answers (AnswerRange)
  */
case class NumericQuestion(id: Option[Long],
                           categoryId: Option[Long],
                           title: String,
                           text: String,
                           explanationText: String,
                           rightAnswerText: String,
                           wrongAnswerText: String,
                           courseId: Long,
                           parentQuestionId: Option[Long] = None)
  extends Question {
  def questionType = QuestionType.Numeric
}

/**
Related data:
  answers (AnswerText)
  */
case class PositioningQuestion(id: Option[Long],
                               categoryId: Option[Long],
                               title: String,
                               text: String,
                               explanationText: String,
                               rightAnswerText: String,
                               wrongAnswerText: String,
                               forceCorrectCount: Boolean,
                               courseId: Long)
  extends Question {
  def questionType = QuestionType.Positioning
}

/**
Related data:
  answers (AnswerKeyValue)
  */
case class MatchingQuestion(id: Option[Long],
                            categoryId: Option[Long],
                            title: String,
                            text: String,
                            explanationText: String,
                            rightAnswerText: String,
                            wrongAnswerText: String,
                            courseId: Long)
  extends Question {
  def questionType = QuestionType.Matching
}

/**
Related data: no
  */
case class EssayQuestion(id: Option[Long],
                         categoryId: Option[Long],
                         title: String,
                         text: String,
                         explanationText: String,
                         courseId: Long)
  extends Question {
  def questionType = QuestionType.Essay
}

/**
Related data:
  answers (AnswerKeyValue)
  */
case class CategorizationQuestion(id: Option[Long],
                                  categoryId: Option[Long],
                                  title: String,
                                  text: String,
                                  explanationText: String,
                                  rightAnswerText: String,
                                  wrongAnswerText: String,
                                  courseId: Long)
  extends Question {
  def questionType = QuestionType.Categorization
}

/**
Related data:
  other questions of one of this types: Choice, Text, Numeric
  */
/*case class ClozeQuestion(id: Option[Long],
                                  categoryId: Option[Long],
                                  title: String,
                                  text: String,
                                  explanationText: String,
                                  courseId: Long)
  extends Question {
  def questionType = QuestionType.Cloze
}*/


