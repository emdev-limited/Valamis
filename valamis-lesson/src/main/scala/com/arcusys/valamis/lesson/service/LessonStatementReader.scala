package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.lesson.model.{BaseManifest, LessonType, PackageBase}
import com.arcusys.valamis.lrs.model.StatementFilter
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.tincan.{Agent, Statement}
import com.arcusys.valamis.lrs.util.StatementApiHelpers._
import com.arcusys.valamis.lrs.util.{TinCanVerbs, TincanHelper}
import com.arcusys.valamis.lrs.util.TincanHelper._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

/**
 * Created by mminin on 22.09.15.
 */
class LessonStatementReader(implicit val bindingModule: BindingModule) extends Injectable {

  private lazy val lrsClient = inject[LrsClientManager]
  private lazy val packageService = inject[ValamisPackageService]

  def getLastAttempted(userId: Long, pack: BaseManifest): Option[Statement] = {
    val activityId = getActivityId(pack)
    val agent = getAgent(userId)
    getLastAttempted(agent, activityId)
  }

  def getLastAttemptedTincan(user: LUser, packageId: Long): Option[Statement] = {
    val agent = getAgent(user)
    val activityId = packageService.getTincanRootActivityId(packageId)
    getLastAttempted(agent, activityId)
  }
  
  def getLastAttempted(user: LUser, activityId: String): Option[Statement] = {
    val agent = getAgent(user)
    getLastAttempted(agent, activityId)
  }

  def getLastAttempted(agent: Agent, activityId: String): Option[Statement] = {
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Attempted)),
      limit = Some(1)
    ))).headOption
  }

  def getAttempted(user: LUser, activityId: String) = {
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(user.getAgentByUuid),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Attempted))
    )))
  }

  def getCompleted(user: LUser, activityId: String) = {
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(user.getAgentByUuid),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Completed))
    )))
  }

  def hasCompletedSuccess(user: LUser, activityId: String): Boolean = {
    val agent = getAgent(user)

    val completedExist = lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Completed))
    )))
      .exists(s => s.result.exists(r => r.success.contains(true) || r.completion.contains(true)))

    if (!completedExist) {
      lrsClient.statementApi(_.getByFilter(StatementFilter(
        agent = Some(agent),
        activity = Some(activityId),
        verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Passed))
      )))
        .exists(s => s.result.exists(r => r.success.contains(true) || r.completion.contains(true)))
    } else completedExist

  }

  def getCompletedSuccessTincan(userId: Long, packageId: Long, after: DateTime): Seq[Statement] = {
    val activityId = packageService.getTincanRootActivityId(packageId)
    val agent = getAgent(userId)

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Completed)),
      since = Some(after.toDate)
    )))
      .filter(s => s.result.isDefined && s.result.get.success.getOrElse(false))
  }
  
  def getCompletedForAttempt(user: LUser, activityId: String, attemptedStatement: Statement): Option[Statement] = {
    val agent = getAgent(user)
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(attemptedStatement.id.get.toString),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Completed)),
      relatedActivities = Some(true),
      limit = Some(1)
    ))) headOption
  }

  def getAll(userId: Long, packageId: Long, limit: Int = 25) = {
    val activityId = packageService.getRootActivityId(packageId)
    val agent = getAgent(userId)
    
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      relatedActivities = Some(true),
      limit = Some(limit)
    )))
  }

  def getAnsweredByPackageId(userId: Long, packageId: Long, limit: Int = 25) = {
    val activityId = packageService.getRootActivityId(packageId)
    val agent = getAgent(userId)
    
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      verb = Some(TinCanVerbs.getVerbURI(TinCanVerbs.Answered)),
      relatedActivities = Some(true),
      limit = Some(limit)
    )))
  }

  def getRoot(userId: Long, packageId: Long, limit: Int = 25) = {
    val activityId = packageService.getRootActivityId(packageId)
    val agent = getAgent(userId)
    
    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      limit = Some(limit)
    )))
  }

  def getLast(userId: Long, pack: PackageBase): Option[Statement] = {
    val activityId = getActivityId(pack)
    val agent = getAgent(userId)

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      relatedActivities = Some(true),
      limit = Some(1)
    ))) headOption
  }

  def getLast(userId: Long, pack: BaseManifest): Option[Statement] = {
    val activityId = getActivityId(pack)
    val agent = getAgent(userId)

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(activityId),
      relatedActivities = Some(true),
      limit = Some(1)
    ))) headOption
  }
  
  private def getActivityId(manifest: BaseManifest): String = {
    manifest.getType match {
      case LessonType.Scorm => packageService.getScormRootActivityId(manifest.id)
      case LessonType.Tincan => packageService.getTincanRootActivityId(manifest.id)
    }
  }

  private def getActivityId(pack: PackageBase): String = {
    pack.packageType match {
      case LessonType.Scorm => packageService.getScormRootActivityId(pack.id)
      case LessonType.Tincan => packageService.getTincanRootActivityId(pack.id)
    }
  }
  
  private def getAgent(userId: Long): Agent = {
    UserLocalServiceHelper().getUser(userId).getAgentByUuid
  }

  private def getAgent(user: LUser): Agent = {
    user.getAgentByUuid
  }
}
