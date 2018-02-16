package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.MessageBusHelper.sendSynchronousMessage
import com.arcusys.valamis.certificate.model.{CertificateStatuses, LPInfo, LPInfoWithUserStatus, LearningPathWithVersion}
import com.arcusys.valamis.util.serialization.DateTimeSerializer
import com.arcusys.valamis.utils.MessageBusExtension
import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.ext.{EnumNameSerializer, PeriodSerializer}
import org.json4s.jackson.JsonMethods

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by amikhailov on 31/03/2017.
  */
class LearningPathServiceImpl extends LearningPathService with JsonMethods with MessageBusExtension {
  private lazy val learningPathDestination = "valamis/learningPath"

  implicit val jsonFormats = DefaultFormats +
    DateTimeSerializer + new EnumNameSerializer(CertificateStatuses) + PeriodSerializer

  override def isLearningPathDeployed: Boolean = {
    val messageValues = prepareMessageData(
      Map(LPMessageFields.Action -> LPMessageActions.IsDeployed)
    )

    sendSynchronousMessage(learningPathDestination, messageValues)
      .toOption contains "true"
  }

  override def getUsersToCertificateCount(since: DateTime,
                                          until: DateTime,
                                          companyId: Long): Future[Map[Long, Int]] = Future {

    sendMessage(learningPathDestination,
      Map(
        LPMessageFields.Action -> LPMessageActions.UsersToLPCount,
        LPMessageFields.StartDate -> since.toString(),
        LPMessageFields.EndDate -> until.toString(),
        LPMessageFields.CompanyId -> companyId.toString
      )
    ) map (_.extract[Map[Long, Int]]) getOrElse Map()
  }

  override def getLearningPathById(id: Long, companyId: Long): Future[Option[LearningPathWithVersion]] = Future {
    sendMessage(learningPathDestination,
      Map(
        LPMessageFields.Action -> LPMessageActions.GetLPById,
        LPMessageFields.Id -> id.toString,
        LPMessageFields.CompanyId -> companyId.toString
      )) map (_.extract[LearningPathWithVersion])
  }

  def getLearningPathsByIds(ids: Seq[Long]): Future[Seq[LPInfo]] = Future {
    if (ids.isEmpty) {
      Seq()
    } else {
      sendMessage(learningPathDestination,
        Map(
          LPMessageFields.Action -> LPMessageActions.GetLPByIds,
          LPMessageFields.Ids -> ids.mkString(",")
        )) map (_.extract[Seq[LPInfo]]) getOrElse Seq()
    }
  }

  override def getLearningPathsWithUserStatusByIds(ids: Seq[Long], userId: Long): Future[Seq[LPInfoWithUserStatus]] = Future {
    if (ids.isEmpty) {
      Seq()
    } else {
      sendMessage(learningPathDestination,
        Map(
          LPMessageFields.Action -> LPMessageActions.GetLPWithStatusByIds,
          LPMessageFields.UserId -> userId.toString,
          LPMessageFields.Ids -> ids.mkString(",")
        )) map (_.extract[Seq[LPInfoWithUserStatus]]) getOrElse Seq()
    }
  }

  override def getPassedLearningPaths(userId: Long, companyId: Long): Future[Seq[LPInfoWithUserStatus]] = Future {
    sendMessage(learningPathDestination,
      Map(
        LPMessageFields.Action -> LPMessageActions.GetPassedLP,
        LPMessageFields.UserId -> userId.toString,
        LPMessageFields.CompanyId -> companyId.toString
      )) map (_.extract[Seq[LPInfoWithUserStatus]]) getOrElse Seq()
  }
}

object LPMessageActions {
  val IsDeployed = "idDeployed"
  val UsersToLPCount = "usersToLPCount"
  val GetLPById = "getLPById"
  val GetLPByIds = "getLPByIds"
  val GetLPWithStatusByIds = "getLPWithStatusByIds"
  val GetPassedLP = "getPassedLP"
}

object LPMessageFields {
  val Action = "action"
  val StartDate = "startDate"
  val EndDate = "endDate"
  val CompanyId = "companyId"
  val UserId = "userId"
  val Id = "id"
  val Ids = "ids"
}