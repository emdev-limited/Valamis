package com.arcusys.learn.liferay.activity

import java.util.UUID

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LBaseSocialActivityInterpreter
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.StringPoolHelper
import com.arcusys.valamis.lrs.api.FailureRequestException
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.arcusys.valamis.lrs.service.{LrsClientManager, LrsRegistration}
import com.arcusys.valamis.lrs.tincan.{Activity, AuthorizationScope, Statement}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portal.kernel.log.LogFactoryUtil

import scala.util.{Failure, Success}

object StatementActivityInterpreter {
  val _CLASS_NAMES = Seq[String](classOf[Statement].getName).toArray
}

class StatementActivityInterpreter extends LBaseSocialActivityInterpreter with Injectable {
  val logger = LogFactoryUtil.getLog(getClass)

  implicit lazy val bindingModule = Configuration
  lazy val lrsRegistration = inject[LrsRegistration]
  lazy val lrsReader = inject[LrsClientManager]

  override def getClassNames = StatementActivityInterpreter._CLASS_NAMES

  override protected def doInterpret(activity: LSocialActivity, context: Context): LSocialActivityFeedEntry = {

    val forUser = getUser(context)
    val creatorUserName = getUserName(activity.getUserId, context)

    //activity extra data contains statementId or statement json
    val statementOpt: Option[Statement] = if (isUUID(activity.getExtraData)) {
      getStatementById(UUID.fromString(activity.getExtraData))
    } else {
      Some(JsonHelper.fromJson[Statement](activity.getExtraData, new StatementSerializer))
    }

    statementOpt match {
      case None =>
        val sb = new StringBuilder
        sb.append(creatorUserName + " ")
        new LSocialActivityFeedEntry(sb.toString(), StringPoolHelper.BLANK)

      case Some(statement) =>
        val activityName = statement.obj match {
          case activity: Activity =>
            // receive activity name from lang map for user locale, or get first
            activity.name
              .filter(_.nonEmpty)
              .map(n => n.getOrElse(forUser.getLanguageId, n.head._2))
              .getOrElse("Unknown activity name")
          case _ => ""
        }
        val verbName = statement.verb.display.getOrElse(forUser.getLanguageId, statement.verb.display.head._2)

        val sb = new StringBuilder
        sb.append(creatorUserName + " ")
        sb.append(verbName + " ")
        sb.append(activityName + " ")
        new LSocialActivityFeedEntry(sb.toString(), StringPoolHelper.BLANK)
    }

  }


  private def isUUID(text: String) = {
    text.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
  }

  private def getStatementById(statementId: UUID) = {
    val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All).auth

    lrsReader.statementApi(_.getStatementById(statementId), Some(lrsAuth)) match {
      case Failure(e: FailureRequestException) if e.responseCode == 404 =>
        logger.warn(s"Statement with id: $statementId not found")
        None

      case Failure(e: FailureRequestException) =>
        logger.error(e)
        None

      case Success(statement) => Some(statement)
    }
  }
}
