package com.arcusys.valamis.course

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services._
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

import scala.collection.JavaConverters.asScalaBufferConverter

/**
  * Created By:
  * User: zsoltberki
  * Date: 4.5.2016
  */
trait CourseMemberService {
  val ACCEPTED = MembershipRequestServiceHelper.STATUS_APPROVED
  val REJECTED = MembershipRequestServiceHelper.STATUS_DENIED
  val PENDING = MembershipRequestServiceHelper.STATUS_PENDING


  def addMembers(courseId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit

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

  def addMembershipRequest(courseId: Long, memberId: Long, comments: String) : Unit
  def removeMembershipRequest(courseId: Long, memberId: Long) : Unit
  def handleMembershipRequest(courseId: Long, memberId: Long, handlingUserId: Long, comment: String, accepted: Boolean) : Unit
  def getPendingMembershipRequests(courseId: Long, ascending: Boolean, skipTake: Option[SkipTake]) : RangeResult[LUser]
  def getPendingMembershipRequestsCount(courseId: Long) : Int
  def getPendingMembershipRequestUserIds(courseId: Long) : Seq[Long]
}

class CourseMemberServiceImpl(implicit val bindingModule: BindingModule)
  extends CourseMemberService
  with Injectable {

  private lazy val memberService = inject[MemberService]
  private lazy val requestService = MembershipRequestServiceHelper

  override def addMembers(courseId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit = {
    memberType match {
      case MemberTypes.User => UserLocalServiceHelper().addGroupUsers(courseId, memberIds)
      case MemberTypes.UserGroup => UserGroupLocalServiceHelper.addGroupUserGroups(courseId, memberIds)
      case MemberTypes.Organization => OrganizationLocalServiceHelper.addGroupOrganizations(courseId,memberIds)
      case MemberTypes.Role => //Do nothing
    }
  }

  override def removeMembers(courseId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit = {
    memberType match {
      case MemberTypes.User => UserLocalServiceHelper().deleteGroupUsers(courseId, memberIds.toArray)
      case MemberTypes.UserGroup => UserGroupLocalServiceHelper.deleteGroupUserGroups(courseId, memberIds.toArray)
      case MemberTypes.Organization => OrganizationLocalServiceHelper.deleteGroupOrganizations(courseId, memberIds.toArray)
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

    if (memberIds.isEmpty){
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
    val memberIds = getCourseMemberIds(courseId, MemberTypes.User)

    if (memberIds.isEmpty){
      RangeResult(0, Nil)
    }
    else {
      memberService.getUserMembers(memberIds, contains = true, companyId, nameFilter, ascending, skipTake, organizationId)
    }
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
      case MemberTypes.Organization => OrganizationLocalServiceHelper.getGroupOrganizations(courseId).asScala.map(_.getOrganizationId)
      case MemberTypes.Role => Seq()
    }
  }

  override def addMembershipRequest(courseId: Long, memberId: Long, comments: String): Unit = {
    requestService.addRequest(courseId,memberId,comments)
  }

  override def removeMembershipRequest(courseId: Long, memberId: Long): Unit = ???

  override def handleMembershipRequest(courseId: Long, memberId: Long, handlingUserId: Long, comment: String, accepted: Boolean): Unit = {
    requestService.getUsersRequests(courseId,memberId,PENDING)
      .map(_.getMembershipRequestId)
      .foreach(requestService.updateStatus(_,handlingUserId,comment,if(accepted) ACCEPTED else REJECTED))
  }

  override def getPendingMembershipRequests(courseId: Long, ascending: Boolean, skipTake: Option[SkipTake]): RangeResult[LUser] = {
    val startEnd = skipTake.map(x => (x.skip, x.skip + x.take))
    lazy val companyId = CompanyHelper.getCompanyId

    val userIds = requestService.getRequests(courseId,PENDING).map(_.getUserId)

    if(userIds.isEmpty) {
      RangeResult(0, Nil)
    }
    else {
      val total = userIds.length
      val users = UserLocalServiceHelper().getUsers(userIds, contains = true, companyId, None, ascending, startEnd, None)
      RangeResult(total, users)
    }
  }

  override def getPendingMembershipRequestUserIds(courseId: Long): Seq[Long] = requestService.getRequests(courseId,PENDING).map(_.getUserId)

  override def getPendingMembershipRequestsCount(courseId: Long): Int = requestService.getRequestCount(courseId, PENDING)
}
