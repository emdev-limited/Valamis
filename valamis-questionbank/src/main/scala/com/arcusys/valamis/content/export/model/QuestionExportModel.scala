package com.arcusys.valamis.content.export.model

case class QuestionCategoryExport(title: String,
                                  description: String,
                                  children: Seq[QuestionExport],
                                  childrenCats: Seq[QuestionCategoryExport],
                                  categoryType: String = "folder",
                                  arrangementIndex: Int = 0)

case class QuestionExport(entityType: String,
                          title: String,
                          text: String,
                          explanationText: String,
                          rightAnswerText: Option[String],
                          wrongAnswerText: Option[String],
                          forceCorrectCount: Boolean,
                          isCaseSensitive: Boolean,
                          answers: Seq[AnswerExport],
                          questionType: Int,
                          arrangementIndex: Int = 0)

case class AnswerExport(answerText: String = "",
                        isCorrect: Boolean = false,
                        rangeFrom: BigDecimal = 0,
                        rangeTo: BigDecimal = 0,
                        matchingText: String = "",
                        score: Option[Double] = None)
