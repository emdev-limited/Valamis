package com.arcusys.learn.liferay.update.version260.model

case class LFQuiz(
                    id: Long,
                    title: Option[String],
                    description: Option[String],
                    logo: Option[String],
                    welcomePageContent: Option[String],
                    finalPageContent: Option[String],
                    courseId: Option[Long],
                    maxDuration: Option[Long])

case class  LFQuizQuestion(
                    id: Long,
                    quizId: Option[Long],
                    categoryId: Option[Long],
                    questionId: Option[Long],
                    questionType: Option[String],
                    title: Option[String],
                    url: Option[String],
                    plaintext: Option[String],
                    arrangementIndex: Option[Long],
                    autoShowAnswer: Option[Boolean],
                    groupId: Option[Long])