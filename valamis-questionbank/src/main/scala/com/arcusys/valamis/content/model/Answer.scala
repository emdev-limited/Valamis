package com.arcusys.valamis.content.model

import com.arcusys.valamis.content.model.AnswerType.AnswerType

object AnswerType extends Enumeration {
  type AnswerType = Value
  val Text = Value("text")
  val Range = Value("range")
  val KeyValue = Value("keyValue")
}

trait Answer {
  def id: Option[Long]
  def questionId: Option[Long]
  def courseId: Long
  def score: Option[Double]
  def answerType:AnswerType
}

case class AnswerText(id: Option[Long],
                        questionId: Option[Long],
                        courseId: Long,
                        body: String,
                        isCorrect: Boolean,
                        position:Int,
                        score: Option[Double] = None
                        ) extends Answer {
  override val answerType = AnswerType.Text
}

case class AnswerRange(id: Option[Long],
                       questionId: Option[Long],
                       courseId: Long,
                       rangeFrom: Double,
                       rangeTo: Double,
                       score: Option[Double] = None) extends Answer {
  override val answerType = AnswerType.Range
}

case class AnswerKeyValue(id: Option[Long],
                          questionId: Option[Long],
                          courseId: Long,
                          key: String,
                          value: Option[String],
                          score: Option[Double] = None) extends Answer {
  override val answerType = AnswerType.KeyValue
}