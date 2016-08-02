package com.arcusys.valamis.gradebook.model

import com.arcusys.valamis.user.model.UserInfo
import org.joda.time.DateTime

case class AttemptInfo(id: String,
                       activity: String,
                       verb: String,
                       verbName: Map[String, String],
                       date: DateTime,
                       statements: Seq[StatementInfo],
                       comments: Seq[CommentInfo])

case class StatementInfo(id: String,
                         activity: String,
                         verb: String,
                         verbName: Map[String, String],
                         description: Option[String],
                         userResponse: Option[String],
                         questionType: Option[String],
                         success: Boolean,
                         score: Option[Float],
                         duration: String,
                         correctAnswer: Option[String],
                         date: DateTime,
                         comments: Seq[CommentInfo])

case class CommentInfo(id: String,
                       verb: String,
                       verbName: Map[String, String],
                       user: Option[UserInfo],
                       userName: Option[String],
                       userResponse: Option[String],
                       date: DateTime)
