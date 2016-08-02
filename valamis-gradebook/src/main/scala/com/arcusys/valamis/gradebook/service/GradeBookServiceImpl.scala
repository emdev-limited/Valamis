package com.arcusys.valamis.gradebook.service

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.gradebook.model.{AttemptInfo, CommentInfo, StatementInfo}
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader}
import com.arcusys.valamis.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.util.Joda._

abstract class GradeBookServiceImpl extends GradeBookService {

  def statementReader: LessonStatementReader
  def lessonService: LessonService

  def getStatementGrades(packageId: Long,
                         valamisUserId: Long,
                         sortAsc: Boolean = false,
                         shortMode: Boolean = false): Seq[Statement] = {

    val statements = if (!shortMode) {
      statementReader.getAll(valamisUserId, packageId)
    }
    else {
      val root = statementReader.getRoot(valamisUserId, packageId)
      val answered = statementReader.getAnsweredByPackageId(valamisUserId, packageId)

      (root ++ answered).sortBy(_.timestamp).reverse
    }


    if (!sortAsc)
      statements.reverse
    else
      statements
  }

  def getAttemptsAndStatements(userId: Long,
                               lessonId: Long,
                               companyId: Long,
                               skipTake: Option[SkipTake]): RangeResult[AttemptInfo] = {

    val activityId = lessonService.getRootActivityId(lessonId)
    val allAttempts = statementReader.getAllAttempts(userId, activityId, 0, 0)

    val verbs = Seq(
      TinCanVerbs.Completed, TinCanVerbs.Passed, TinCanVerbs.Answered,
      TinCanVerbs.Commented, TinCanVerbs.Suspended, TinCanVerbs.Resumed
    )

    val attempts = skipTake match {
      case Some(SkipTake(skip, take)) => allAttempts.slice(skip, skip + take)
      case None => allAttempts
    }

    //TODO: we read all statements, need to improve it
    lazy val allStatements = statementReader.getByActivityId(userId, activityId, verbs)
    lazy val allComments = allStatements
      .filter { s =>
        s.verb.id == TinCanVerbs.getVerbURI(TinCanVerbs.Commented) &&
          s.obj.isInstanceOf[StatementReference]
      }

    val items = attempts.map { attempt =>
      val statements = allStatements
        .map(st => (st, st.obj, st.context.flatMap(_.statement)))
          .collect {
            case (st, obj: Activity, Some(statementRef)) if statementRef.id == attempt.id.get =>
                toStatementInfo(st, obj, allComments, companyId)
          }

      val comments = allComments.map(st => (st, st.obj))
        .collect {
          case (st, obj: StatementReference) if obj.id == attempt.id.get =>
            toCommentInfo(st, companyId)
        }

      toAttemptInfo(attempt, statements, comments)
    }

    RangeResult(allAttempts.size, items)
  }

  private def toAttemptInfo(attempt: Statement, statements: Seq[StatementInfo], comments:Seq[CommentInfo]) = {
    AttemptInfo(
      attempt.id.map(_.toString) getOrElse "",
      attempt.obj.asInstanceOf[Activity].id,
      attempt.verb.id,
      attempt.verb.display,
      attempt.timestamp,
      statements,
      comments
    )
  }

  private def toStatementInfo(statement: Statement,
                              obj: Activity,
                              allComments: Seq[Statement],
                              companyId: Long): StatementInfo = {
    val comments = allComments
      .map(st => (st, st.obj))
      .collect {
        case (st, obj: StatementReference) if obj.id == statement.id.get =>
          toCommentInfo(st, companyId)
      }

    StatementInfo(
      statement.id.map(_.toString) getOrElse "",
      obj.id,
      statement.verb.id,
      statement.verb.display,
      obj.description.flatMap(_.values.headOption),
      statement.result.flatMap(_.response),
      obj.interactionType.map(_.toString),
      statement.result.flatMap(_.success) getOrElse false,
      statement.result.flatMap(_.score).flatMap(_.scaled),
      statement.result.flatMap(_.duration) getOrElse "",
      obj.correctResponsesPattern.headOption,
      statement.timestamp,
      comments
    )
  }

  private def toCommentInfo(comment: Statement, companyId: Long): CommentInfo = {

    val user = comment.actor.account match {
      case Some(account: Account) =>
        Option(UserLocalServiceHelper().fetchUserByUuidAndCompanyId(account.name, companyId))
      case _ =>
        comment.actor.mBox flatMap {
          email => Option(UserLocalServiceHelper().fetchUserByEmailAddress(companyId, email.replace("mailto:", "")))
        }
    }
    user.map { u =>
      CommentInfo(
        comment.id.map(_.toString) getOrElse "",
        comment.verb.id,
        comment.verb.display,
        Some(new UserInfo(u)),
        None,
        comment.result.flatMap(_.response),
        comment.timestamp
      )
    } getOrElse CommentInfo(
      comment.id.map(_.toString) getOrElse "",
      comment.verb.id,
      comment.verb.display,
      None,
      comment.actor.account.map(_.name),
      comment.result.flatMap(_.response),
      comment.timestamp
    )
  }

}
