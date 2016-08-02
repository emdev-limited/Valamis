package com.arcusys.valamis.content.export.model

import com.arcusys.valamis.content.model.QuestionType.QuestionType
import com.arcusys.valamis.content.model._

/**
 *
 * Created by pkornilov on 15.10.15.
 */
class MoodleAnswer(
                    var text: String = "",
                    var feedback: String = "",
                    var tolerance: Double = 0,
                    var fraction: Int = 0,
                    var matchingElement: String = "",
                    var format: Option[String] = None
                    ) {

  def toValamisAnswer(qType: QuestionType, courseId: Long): Answer = qType match {

    case QuestionType.Choice | QuestionType.Text | QuestionType.Positioning =>
      //TODO Answer score
      AnswerText(id = None,
        questionId = None,
        courseId = courseId,
        body = text,
        isCorrect = fraction > 0,
        position = 0, //Moodle doesn't have positioning questions, so this field is always 0 here
        score = None)
    case QuestionType.Numeric =>
      AnswerRange(
        id = None,
        questionId = None,
        courseId = courseId,
        rangeFrom = text.toDouble - tolerance,
        rangeTo = text.toDouble + tolerance,
        score = None
      )
    case QuestionType.Matching | QuestionType.Categorization =>
      AnswerKeyValue(
        id = None,
        questionId = None,
        courseId = courseId,
        key = text,
        value = Some(matchingElement),
        score = None
      )

  }

}
