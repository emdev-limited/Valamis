package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.MessageBusHelper._
import com.arcusys.valamis.certificate.AssignmentSort
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.serializer.AssignmentSerializer
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

trait AssignmentService {
  def isAssignmentDeployed: Boolean
  def getBy(filter: AssignmentFilter, skipTake: Option[SkipTake]): RangeResult[Assignment]
  def getById(assignmentId: Long): Option[Assignment]
  def getAssignmentUsers(assignmentId: Long, skipTake: Option[SkipTake]): RangeResult[AssignmentUserInfo]
  def getUserAssignments(userId: Long,
                         groupId: Option[Long] = None,
                         skipTake: Option[SkipTake] = None,
                         sortBy: Option[AssignmentSort] = None): RangeResult[Assignment]
  def getSubmissionStatus(assignmentId: Long, userId: Long): Option[UserStatuses.Value]
  def getEvaluationDate(assignmentId: Long, userId: Long): Option[DateTime]
}

class AssignmentServiceImpl extends AssignmentService with AssignmentSerializer {
  private lazy val assignmentDestination = "valamis/assignment"
  private lazy val assignmentDeployedDestination = "valamis/main/assignmentDeployed"
  private lazy val assignmentMainDestination = Some("valamis/main/assignment")

  override def isAssignmentDeployed: Boolean = {
    val messageValues = prepareMessageData(Map("action" -> AssignmentMessageActionType.Check.toString))

    sendSynchronousMessage(assignmentDestination,
      Some(assignmentDeployedDestination),
      messageValues
    ).toOption.contains("deployed")
  }

  override def getBy(filter: AssignmentFilter, skipTake: Option[SkipTake]): RangeResult[Assignment] = {
    val messageValues = prepareMessageData(Map(
      AssignmentMessageFields.Action -> AssignmentMessageActionType.List.toString
    ))
    for(groupId <- filter.groupId)
      messageValues.put(AssignmentMessageFields.GroupId, groupId.toString)
    for(titlePattern <- filter.titlePattern)
      messageValues.put(AssignmentMessageFields.TitlePattern, titlePattern)
    for(SkipTake(skip, take) <- skipTake) {
      messageValues.put(AssignmentMessageFields.Skip, skip.toString)
      messageValues.put(AssignmentMessageFields.Take, take.toString)
    }
    for(sortBy <- filter.sortBy) {
      messageValues.put(AssignmentMessageFields.SortBy, sortBy.sortBy.toString.toLowerCase)
      messageValues.put(AssignmentMessageFields.Order, sortBy.order.toString.toLowerCase)
    }
    for(status <- filter.status)
      messageValues.put(AssignmentMessageFields.Status, status.toString)

    handleMessageResponse[RangeResult[Assignment]](
      sendSynchronousMessage(assignmentDestination, assignmentMainDestination, messageValues),
      deserializeAssignmentList
    ).getOrElse(RangeResult(0, Seq()))
  }

  override def getById(assignmentId: Long): Option[Assignment] = {
    val messageValues = prepareMessageData(Map(
      AssignmentMessageFields.Action -> AssignmentMessageActionType.ById.toString,
      AssignmentMessageFields.AssignmentId -> assignmentId.toString
    ))

    handleMessageResponse[Assignment](
      sendSynchronousMessage(assignmentDestination, assignmentMainDestination, messageValues),
      deserializeAssignment
    )
  }

  override def getAssignmentUsers(assignmentId: Long, skipTake: Option[SkipTake]): RangeResult[AssignmentUserInfo] = {
    val messageValues = prepareMessageData(Map(
      AssignmentMessageFields.Action -> AssignmentMessageActionType.AssignmentUsers.toString,
      AssignmentMessageFields.AssignmentId -> assignmentId.toString
    ))
    for(SkipTake(skip, take) <- skipTake) {
      messageValues.put(AssignmentMessageFields.Skip, skip.toString)
      messageValues.put(AssignmentMessageFields.Take, take.toString)
    }

    handleMessageResponse[RangeResult[AssignmentUserInfo]](
      sendSynchronousMessage(assignmentDestination, assignmentMainDestination, messageValues),
      deserializeUserList
    ).getOrElse(RangeResult(0, Seq()))
  }

  override def getUserAssignments(userId: Long,
                                  groupId: Option[Long] = None,
                                  skipTake: Option[SkipTake] = None,
                                  sortBy: Option[AssignmentSort] = None): RangeResult[Assignment] = {
    val messageValues = prepareMessageData(Map(
      AssignmentMessageFields.Action -> AssignmentMessageActionType.UserAssignments.toString,
      AssignmentMessageFields.UserId -> userId.toString
    ))
    for(gId <- groupId) {
      messageValues.put(AssignmentMessageFields.GroupId, gId.toString)
    }
    for(SkipTake(skip, take) <- skipTake) {
      messageValues.put(AssignmentMessageFields.Skip, skip.toString)
      messageValues.put(AssignmentMessageFields.Take, take.toString)
    }
    for(sort <- sortBy) {
      messageValues.put(AssignmentMessageFields.SortBy, sort.sortBy.toString.toLowerCase)
      messageValues.put(AssignmentMessageFields.Order, sort.order.toString.toLowerCase)
    }

    handleMessageResponse[RangeResult[Assignment]](
      sendSynchronousMessage(assignmentDestination, assignmentMainDestination, messageValues),
      deserializeAssignmentList
    ).getOrElse(RangeResult(0, Seq()))
  }

  override def getSubmissionStatus(assignmentId: Long, userId: Long): Option[UserStatuses.Value] = {
    val messageValues = prepareMessageData(Map(
      AssignmentMessageFields.Action -> AssignmentMessageActionType.SubmissionStatus.toString,
      AssignmentMessageFields.AssignmentId -> assignmentId.toString,
      AssignmentMessageFields.UserId -> userId.toString
    ))

    handleMessageResponse[UserStatuses.Value](
      sendSynchronousMessage(assignmentDestination, assignmentMainDestination, messageValues),
      UserStatuses.withName
    )
  }

  override def getEvaluationDate(assignmentId: Long, userId: Long): Option[DateTime] = {
    val messageValues = prepareMessageData(Map(
      AssignmentMessageFields.Action -> AssignmentMessageActionType.EvaluationDate.toString,
      AssignmentMessageFields.AssignmentId -> assignmentId.toString,
      AssignmentMessageFields.UserId -> userId.toString
    ))

    handleMessageResponse[DateTime](
      sendSynchronousMessage(assignmentDestination, assignmentMainDestination, messageValues),
      DateTime.parse
    )
  }

  private def prepareMessageData(data: Map[String, String]): java.util.HashMap[String, AnyRef] = {
    val messageValues = new java.util.HashMap[String, AnyRef]()
    data.keys.map(k => messageValues.put(k, data(k)))

    messageValues
  }

  private def handleMessageResponse[T](response: Try[Object], convert: (String) => T): Option[T] = {
    response match {
      case Success(json: String) =>
        if(json.nonEmpty) {
          Some(convert(json))
        } else None
      case Success(value) => throw new Exception(s"Unsupported response: $value")
      case Failure(ex) => throw new Exception(ex)
    }
  }
}