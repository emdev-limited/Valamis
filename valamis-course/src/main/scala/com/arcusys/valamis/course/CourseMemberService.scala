package com.arcusys.valamis.course

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services._
import com.arcusys.valamis.course.service.{CourseUserQueueService, CourseUserQueueServiceImpl}
import com.arcusys.valamis.course.storage.CourseExtendedRepository
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created By:
  * User: zsoltberki
  * Date: 4.5.2016
  */
trait CourseMemberService {
  val ACCEPTED = MembershipRequestServiceHelper.STATUS_APPROVED
  val REJECTED = MembershipRequestServiceHelper.STATUS_DENIED
  val PENDING = MembershipRequestServiceHelper.STATUS_PENDING


  def addUser(courseId: Long, userId: Long): Unit

  def addUsers(courseId: Long, userIds: Seq[Long]): Unit

  def removeMembers(courseId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit

  def getMembers(courseId: Long,
                 memberType: MemberTypes.Value,
                 nameFilter: Option[String],
                 ascending: Boolean,
                 skipTake: Option[SkipTake]): RangeResult[Member]

  def getUserMembers(courseId: Long,
                     nameFilter: Option[String],
                     ascending: Boolean,
                     skipTake: Option[SkipTake],
                     organizationId: Option[Long]): RangeResult[LUser]

  def getAvailableMembers(courseId: Long,
                          memberType: MemberTypes.Value,
                          nameFilter: Option[String],
                          ascending: Boolean,
                          skipTake: Option[SkipTake]): RangeResult[Member]

  def getAvailableUserMembers(courseId: Long,
                              nameFilter: Option[String],
                              ascending: Boolean,
                              skipTake: Option[SkipTake],
                              organizationId: Option[Long]): RangeResult[LUser]

  def addMembershipRequest(courseId: Long, memberId: Long, comments: String): Unit
  def removeMembershipRequest(courseId: Long, memberId: Long): Unit
  def handleMembershipRequest(courseId: Long, memberId: Long, handlingUserId: Long, comment: String, accepted: Boolean): Unit
  def getPendingMembershipRequests(courseId: Long, ascending: Boolean, skipTake: Option[SkipTake]): RangeResult[LUser]
  def getPendingMembershipRequestsCount(courseId: Long): Int
  def getPendingMembershipRequestUserIds(courseId: Long): Seq[Long]

  def getCountUsers(courseId: Long): Int
}

//TODO: return Future to awoid using 'await'
abstract class CourseMemberServiceImpl extends CourseMemberService {

  def memberService: MemberService
  def courseNotification: CourseNotificationService
  def courseRepository: CourseExtendedRepository
  def courseUserQueueService: CourseUserQueueService
  def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  private lazy val requestService = MembershipRequestServiceHelper

  override def addUser(courseId: Long, userId: Long): Unit = {
    val limit = courseRepository.getById(courseId).flatMap(_.userLimit)
    val userCount = getCountUsers(courseId)
    val isFullCourse = limit.exists(_ <= userCount)

    if (isFullCourse) {
      addToQueue(courseId, userId)
    } else {
      deleteFromQueue(courseId, userId)
      UserLocalServiceHelper().addUser(courseId, userId)
    }
  }

  private def addToQueue(courseId: Long, userId: Long) = await {
    for {
      queueItem <- courseUserQueueService.get(courseId, userId)
      _ <- if (queueItem.isEmpty) {
        courseUserQueueService.create(courseId, userId)
      } else {
        Future.successful()
      }
    } yield queueItem
  }

  private def deleteFromQueue(courseId: Long, userId: Long) = await {
    courseUserQueueService.delete(courseId, userId)
  }

  def addUsers(courseId: Long, userIds: Seq[Long]): Unit = {
    val limit = courseRepository.getById(courseId).flatMap(_.userLimit)
    limit match {
      case Some(l) => userIds map (addUser(courseId, _))
      case None => UserLocalServiceHelper().addGroupUsers(courseId, userIds)
    }

  }

  override def removeMembers(courseId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit = {
    memberType match {
      case MemberTypes.User => UserLocalServiceHelper().deleteGroupUsers(courseId, memberIds)
      case MemberTypes.UserGroup => UserGroupLocalServiceHelper.deleteGroupUserGroups(courseId, memberIds)
      case MemberTypes.Organization => OrganizationLocalServiceHelper.deleteGroupOrganizations(courseId, memberIds)
      case MemberTypes.Role => //Do nothing
    }
  }

  override def getMembers(courseId: Long,
                          memberType: MemberTypes.Value,
                          nameFilter: Option[String],
                          ascending: Boolean,
                          skipTake: Option[SkipTake]): RangeResult[Member] = {

    lazy val companyId = CompanyHelper.getCompanyId
    val memberIds = getCourseMemberIds(courseId, memberType)

    if (memberIds.isEmpty) {
      RangeResult(0, Nil)
    }
    else {
      memberService.getMembers(memberIds, contains = true, memberType, companyId, nameFilter, ascending, skipTake)
    }
  }

  override def getAvailableMembers(courseId: Long,
                                   memberType: MemberTypes.Value,
                                   nameFilter: Option[String],
                                   ascending: Boolean,
                                   skipTake: Option[SkipTake]): RangeResult[Member] = {
    lazy val companyId = CompanyHelper.getCompanyId
    val memberIds = getCourseMemberIds(courseId, memberType)

    memberService.getMembers(memberIds, contains = false, memberType, companyId, nameFilter, ascending, skipTake)
  }

  override def getUserMembers(courseId: Long,
                              nameFilter: Option[String],
                              ascending: Boolean,
                              skipTake: Option[SkipTake],
                              organizationId: Option[Long]): RangeResult[LUser] = {

    lazy val companyId = CompanyHelper.getCompanyId

    val groupsIds = getCourseMemberIds(courseId, MemberTypes.UserGroup)
    val memberIds = getCourseMemberIds(courseId, MemberTypes.User)


    if ((memberIds ++ groupsIds).isEmpty) {
      RangeResult(0, Nil)
    } else {
      memberService.getUserAndGroupMembers(memberIds,
        groupsIds,
        contains = true,
        companyId,
        nameFilter,
        ascending,
        skipTake,
        organizationId)
    }
  }

  override def getCountUsers(courseId: Long): Int = {
    val usersIds = UserLocalServiceHelper().getGroupUserIds(courseId)
    val usersIdsFromGroups = UserGroupLocalServiceHelper.getGroupUserGroupsIds(courseId) flatMap { userGroupId =>
      UserLocalServiceHelper().getUserGroupUsersIds(userGroupId)
    }
    val usersIdsFromOrg = OrganizationLocalServiceHelper.getGroupOrganizationsIds(courseId) flatMap { orgId =>
      UserLocalServiceHelper().getOrganizationUsers(orgId).map(_.getUserId)
    }

    (usersIds ++ usersIdsFromGroups ++ usersIdsFromOrg).toSet.size
  }

  override def getAvailableUserMembers(courseId: Long,
                                       nameFilter: Option[String],
                                       ascending: Boolean,
                                       skipTake: Option[SkipTake],
                                       organizationId: Option[Long]): RangeResult[LUser] = {
    lazy val companyId = CompanyHelper.getCompanyId
    val memberIds = getCourseMemberIds(courseId, MemberTypes.User)

    memberService.getUserMembers(memberIds, contains = false, companyId, nameFilter, ascending, skipTake, organizationId)
  }

  def getCourseMemberIds(courseId: Long, memberType: MemberTypes.Value): Seq[Long] = {
    memberType match {
      case MemberTypes.User => UserLocalServiceHelper().getGroupUserIds(courseId)
      case MemberTypes.UserGroup => UserGroupLocalServiceHelper.getGroupUserGroups(courseId).map(_.getUserGroupId)
      case MemberTypes.Organization => OrganizationLocalServiceHelper.getGroupOrganizationsIds(courseId)
      case MemberTypes.Role => Seq()
    }
  }

  override def addMembershipRequest(courseId: Long, memberId: Long, comments: String): Unit = {
    requestService.addRequest(courseId, memberId, comments)
    courseNotification.sendMemberJoinRequest(courseId, memberId)
  }

  override def removeMembershipRequest(courseId: Long, memberId: Long): Unit = ???

  override def handleMembershipRequest(courseId: Long, memberId: Long, handlingUserId: Long, comment: String, accepted: Boolean): Unit = {
    requestService.getUsersRequests(courseId, memberId, PENDING)
      .map(_.getMembershipRequestId)
      .foreach(requestService.updateStatus(_, handlingUserId, comment, if (accepted) ACCEPTED else REJECTED))
  }

  override def getPendingMembershipRequests(courseId: Long, ascending: Boolean, skipTake: Option[SkipTake]): RangeResult[LUser] = {
    val startEnd = skipTake.map(x => (x.skip, x.skip + x.take))
    lazy val companyId = CompanyHelper.getCompanyId

    val userIds = requestService.getRequests(courseId, PENDING).map(_.getUserId)

    if (userIds.isEmpty) {
      RangeResult(0, Nil)
    }
    else {
      val total = userIds.length
      val users = UserLocalServiceHelper().getUsers(userIds, contains = true, companyId, None, ascending, startEnd, None)
      RangeResult(total, users)
    }
  }

  override def getPendingMembershipRequestUserIds(courseId: Long): Seq[Long] = requestService.getRequests(courseId, PENDING).map(_.getUserId)

  override def getPendingMembershipRequestsCount(courseId: Long): Int = requestService.getRequestCount(courseId, PENDING)
}
