package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.CourseUtilHelper
import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.storage.{CertificateGoalStateRepository, CertificateRepository, CertificateStateRepository}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.{Order, RangeResult, SkipTake}
import com.arcusys.learn.liferay.services._
import com.liferay.portal.kernel.util.PrefsPropsUtil
import org.joda.time.DateTime

abstract class CertificateUserServiceImpl extends CertificateUserService {

  def certificateRepository: CertificateRepository
  def certificateToUserRepository: CertificateStateRepository
  def goalStateRepository: CertificateGoalStateRepository
  def checker: CertificateStatusChecker
  def actionSocialActivity = new SocialActivityHelper(CertificateActionType)
  def certificateMemberService: CertificateMemberService
  def certificateBadgeService: CertificateBadgeService
  def certificateGoalService: CertificateGoalService
  def userStatusHistory: UserStatusHistoryService
  def certificateService: CertificateService
  def certificateNotification: CertificateNotificationService

  def addMembers(certificateId: Long,
                 memberIds: Seq[Long],
                 memberType: MemberTypes.Value,
                 isCurrentUser: Boolean = false): Unit = {

    certificateMemberService.addMembers(certificateId, memberIds, memberType)

    val certificate = certificateRepository.getById(certificateId)
    val hasGoals = certificateGoalService.hasGoals(certificateId)

    val userStatus = if (hasGoals || !certificate.isActive) CertificateStatuses.InProgress else CertificateStatuses.Success

    memberType match {
      case MemberTypes.User =>
        memberIds.foreach(addUser(_, userStatus, certificate, isCurrentUser))
      case MemberTypes.UserGroup =>
        memberIds.flatMap(UserLocalServiceHelper().getUserGroupUsersIds)
          .foreach(addUser(_, userStatus, certificate))
      case MemberTypes.Organization =>
        memberIds.flatMap(UserLocalServiceHelper().getOrganizationUserIds)
          .foreach(addUser(_, userStatus, certificate))
      case MemberTypes.Role =>
        memberIds.flatMap(UserLocalServiceHelper().getRoleUserIds)
          .foreach(addUser(_, userStatus, certificate))
    }
  }

  def addUser(userId: Long,
              userStatus: CertificateStatuses.Value,
              certificate: Certificate, isCurrentUser: Boolean): Unit = {
    lazy val now = DateTime.now

    if (certificateToUserRepository.getBy(userId, certificate.id).isEmpty) {
      val status = certificateToUserRepository.create(
        CertificateState(userId, userStatus, now, now, certificate.id)
      )

      userStatusHistory.add(status)

      if (certificate.isActive) checker.checkAndGetStatus(certificate.id, userId)

      if (certificate.isActive && goalStateRepository.getByCertificate(certificate.id, userId).isEmpty) {
        certificateGoalService.updatePackageGoalState(certificate.id, userId)
        certificateGoalService.updateAssignmentGoalState(certificate.id, userId)
      }

      certificateNotification.sendUserAddedNotification(isCurrentUser, certificate, userId)
    }
  }

  def addUserMember(certificateId: Long,
                    userId: Long,
                    courseId: Long): Unit = {
    addMembers(certificateId, Seq(userId), MemberTypes.User, isCurrentUser = true)
    val companyId = CourseUtilHelper.getCompanyId(courseId)

    actionSocialActivity.addWithSet(
      companyId,
      userId,
      courseId = Some(courseId),
      classPK = Some(certificateId),
      `type` = Some(CertificateActivityType.UserJoined.id),
      createDate = DateTime.now
    )
  }

  override def isUserJoined(certificateId: Long, userId: Long): Boolean = {
    certificateToUserRepository.getBy(userId, certificateId).isDefined
  }

  def deleteMembers(certificateId: Long,
                    memberIds: Seq[Long],
                    memberType: MemberTypes.Value): Unit = {

    memberType match {
      case MemberTypes.User =>
        memberIds.foreach(deleteUser(_, certificateId))
      case MemberTypes.UserGroup =>
        memberIds.flatMap(UserLocalServiceHelper().getUserGroupUsersIds)
          .foreach(deleteUser(_, certificateId))
      case MemberTypes.Organization =>
        memberIds.flatMap(UserLocalServiceHelper().getOrganizationUserIds)
          .foreach(deleteUser(_, certificateId))
      case MemberTypes.Role =>
        memberIds.flatMap(UserLocalServiceHelper().getRoleUserIds)
          .foreach(deleteUser(_, certificateId))
    }
    certificateMemberService.removeMembers(certificateId, memberIds, memberType)
  }

  def deleteUser(userId: Long, certificateId: Long): Unit = {
    val status = certificateToUserRepository.getBy(userId, certificateId)

    certificateToUserRepository.delete(userId, certificateId)

    goalStateRepository.deleteBy(certificateId, userId)

    status.foreach (status => userStatusHistory.add(status, isDeleted = true))

  }

  def getByUser(userId: Long,
                filter: CertificateFilter,
                isAchieved: Option[Boolean],
                skipTake: Option[SkipTake]): RangeResult[Certificate] = {

    certificateRepository.getByUser(filter, userId, isAchieved, skipTake)
  }

  def getSuccessByUser(userId: Long, companyId: Long, titlePattern: Option[String]): Seq[Certificate] = {
    certificateRepository.getByState(
      CertificateFilter(companyId, titlePattern),
      CertificateStateFilter(userId = Some(userId), statuses = CertificateStatuses.inProgressAndSuccess)
    )
      .filter(c => checker.checkAndGetStatus(c.id, userId) == CertificateStatuses.Success)
      .sortBy(_.title.toLowerCase)
  }


  def hasUser(certificateId: Long, userId: Long): Boolean = {
    certificateToUserRepository.getBy(userId, certificateId).isDefined
  }

  def getAvailableCertificates(userId:Long,
                               filter: CertificateFilter,
                               skipTake: Option[SkipTake]): RangeResult[Certificate] = {
    certificateRepository.getAvailable(filter, skipTake = skipTake, userId = userId)
  }

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          sortAZ: Boolean,
                                          skipTake: Option[SkipTake],
                                          titlePattern: Option[String]): RangeResult[Certificate] = {

    var certificates = getCertificatesByUserWithOpenBadges(userId, companyId, titlePattern)
    val total = certificates.length

    if (!sortAZ) certificates = certificates.reverse

    for (SkipTake(skip, take) <- skipTake)
      certificates = certificates.slice(skip, skip + take)

    RangeResult(total, certificates)
  }

  private def getCertificatesByUserWithOpenBadges(userId: Long, companyId: Long, titlePattern: Option[String]): Seq[Certificate] = {
    val certificates = getSuccessByUser(userId, companyId, titlePattern)

    val openBadges = getOpenBadges(userId, companyId, titlePattern, certificates)

    certificates ++ openBadges
  }

  def getUsers(c: Certificate): Seq[(DateTime, LUser)] = {
    certificateToUserRepository
      .getBy(CertificateStateFilter(certificateId = Some(c.id)))
      .map(p => (p.userJoinedDate, UserLocalServiceHelper().fetchUser(p.userId)))
      .filter(_._2.isDefined)
      .map(p => (p._1, p._2.get))
  }

  def getCertificatesByUserWithOpenBadgesAndDates(companyId: Long, userId: Long): Seq[(Certificate, Option[CertificateState])] = {
    val certificatesWithState = certificateRepository.getWithUserState(
      companyId,
      userId,
      CertificateStatuses.Success)

    val openBadgesCertificates = getOpenBadges(userId, companyId, None, certificatesWithState.map(_._1))

    certificatesWithState.map { case (c, s) => (c, Some(s)) } ++
      openBadgesCertificates.map { c => (c, None) }
  }

  private def getOpenBadges(userId: Long, companyId: Long, titlePattern: Option[String], certificates: Seq[Certificate]): List[Certificate] = {
    //Backbone squash models with the same ids to one, so we set different negative ids
    certificateBadgeService.getOpenBadges(userId, companyId, titlePattern).zipWithIndex
      .filter { case (badge, index) => !certificates.exists(c => c.title == badge.title) }
      .map { case (badge, index) => badge.copy(id = -index - 1) }
  }

  def getWithStates(userId: Long,
                    companyId: Long,
                    scopeId: Option[Long],
                    statuses: Set[CertificateStatuses.Value]): Seq[(Certificate, CertificateState)] = {

    val sortBy = Some(CertificateSort(CertificateSortBy.Name, Order.Asc))
    val filter = CertificateFilter(companyId, scope = scopeId, isActive = Some(true), sortBy = sortBy)
    val stateFilter = CertificateStateFilter(userId = Some(userId), statuses = statuses)

    val certificates = (if(statuses.contains(CertificateStatuses.Success)) {
      certificateRepository.getByUser(filter, stateFilter, isAchieved = Some(true), skipTake = None).records
    } else {
      certificateRepository.getByState(filter, stateFilter)
    })
      .filter(c => statuses contains checker.checkAndGetStatus(c.id, userId))

    val statesMap = certificateToUserRepository.getBy(userId, certificates.map(_.id))
      .map(state => state.certificateId -> state).toMap

    certificates.map(c => (c, statesMap(c.id)))
  }
}
