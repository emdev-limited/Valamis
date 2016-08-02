package com.arcusys.valamis.certificate.service

import java.security.MessageDigest

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.CourseUtilHelper
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.badge._
import com.arcusys.valamis.certificate.service.util.OpenBadgesHelper
import com.arcusys.valamis.certificate.storage.{CertificateGoalStateRepository, CertificateRepository, CertificateStateRepository}
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.settings.model
import com.arcusys.valamis.settings.model.SettingType
import com.arcusys.valamis.settings.storage.SettingStorage
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.HexHelper
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.DateTime

//TODO refactor, move badge client code to BadgeClient class
trait CertificateUserServiceImpl extends Injectable with CertificateService {

  private lazy val userLocalServiceHelper = inject[UserLocalServiceHelper]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateToUserRepository = inject[CertificateStateRepository]
  private lazy val userService = inject[UserService]
  private lazy val settingStorage = inject[SettingStorage]
  private lazy val goalStateRepository = inject[CertificateGoalStateRepository]
  private lazy val checker = inject[CertificateStatusChecker]
  private lazy val actionSocialActivity = new SocialActivityHelper(CertificateActionType)
  private lazy val certificateMemberService = inject[CertificateMemberService]

  def addMembers(certificateId: Long,
                 memberIds: Seq[Long],
                 memberType: MemberTypes.Value): Unit = {

    certificateMemberService.addMembers(certificateId, memberIds, memberType)

    val (certificate, counts) = certificateRepository.getByIdWithItemsCount(certificateId)
      .getOrElse(throw new EntityNotFoundException(s"no certificate with id: $certificateId"))

    val userStatus = counts match {
      case CertificateItemsCount(_, 0, 0, 0, 0, 0) if certificate.isPublished =>
        CertificateStatuses.Success
      case _ =>
        CertificateStatuses.InProgress
    }

    memberType match {
      case MemberTypes.User =>
        memberIds.foreach(addUser(_, userStatus, certificate))
      case MemberTypes.UserGroup =>
        memberIds.flatMap(UserLocalServiceHelper().getGroupUserIds)
          .foreach(addUser(_, userStatus, certificate))
      case MemberTypes.Organization =>
        memberIds.flatMap(UserLocalServiceHelper().getOrganizationUserIds)
          .foreach(addUser(_, userStatus, certificate))
      case MemberTypes.Role =>
        memberIds.flatMap(UserLocalServiceHelper().getRoleUserIds)
          .foreach(addUser(_, userStatus, certificate))
    }
  }

  private def addUser(userId: Long,
                      userStatus: CertificateStatuses.Value,
                      certificate: Certificate) = {
    lazy val now = DateTime.now

    if (certificateToUserRepository.getBy(userId, certificate.id).isEmpty) {
      certificateToUserRepository.create(
        CertificateState(userId, userStatus, now, now, certificate.id)
      )
      if (certificate.isPublished && goalStateRepository.getByCertificate(certificate.id, userId).isEmpty){
        addPackageGoalState(certificate.id, userId)
        addAssignmentGoalState(certificate.id, userId)
      }
    }
  }

  def addUserMember(certificateId: Long,
                    userId: Long,
                    courseId: Long): Unit = {
    addMembers(certificateId, Seq(userId), MemberTypes.User)
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
        memberIds.flatMap(UserLocalServiceHelper().getGroupUserIds)
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

  private def deleteUser(userId: Long, certificateId: Long): Unit = {
    certificateToUserRepository.delete(userId, certificateId)
    goalStateRepository.deleteBy(certificateId, userId)
  }

  def getForUser(userId: Long,
                 companyId: Long,
                 sortAZ: Boolean,
                 skipTake: Option[SkipTake],
                 titlePattern: Option[String],
                 isPublished: Option[Boolean]): RangeResult[Certificate] = {

    var certificates = getForUser(userId, companyId, isPublished, titlePattern)
      .sortBy(_.title.toLowerCase)
    val total = certificates.length

    if (!sortAZ) certificates = certificates.reverse

    for (SkipTake(skip, take) <- skipTake)
      certificates = certificates.slice(skip, skip + take)

    RangeResult(total, certificates)
  }

  private def getForUser(userId: Long, companyId: Long, isPublished: Option[Boolean], title: Option[String]): Seq[Certificate] = {
    certificateRepository.getByState(
      CertificateFilter(companyId, title, isPublished = isPublished),
      CertificateStateFilter(userId = Some(userId))
    )
  }

  def getSuccessByUser(userId: Long, companyId: Long, titlePattern: Option[String]): Seq[Certificate] = {
    certificateRepository.getByState(
      CertificateFilter(companyId, titlePattern),
      CertificateStateFilter(userId = Some(userId), statuses = CertificateStatuses.inProgressAndSuccess)
    )
      .filter(c => checker.checkAndGetStatus(c.id, userId) == CertificateStatuses.Success)
      .sortBy(_.title.toLowerCase)
  }

  private def getOpenBadges(userId: Long,
                            companyId: Long,
                            titlePattern: Option[String]) = {
    val userEmail = userService.getById(userId).getEmailAddress

    var openbadges = OpenBadgesHelper.getOpenBadges(userEmail)
      .map(x => Certificate(id = -1, title = x("title").toString, description = x("description").toString, logo = x("logo").toString, companyId = companyId, createdAt = DateTime.now))

    if (titlePattern.isDefined) {
      val title = titlePattern.get.toLowerCase
      openbadges = openbadges.filter(_.title.toLowerCase contains title)
    }

    openbadges
      .sortBy(_.title.toLowerCase)
  }

  def hasUser(certificateId: Long, userId: Long): Boolean = {
    certificateToUserRepository.getBy(userId, certificateId).isDefined
  }

  def getUserStatus(certificateId: Long, userId: Long) = {
    checker.checkAndGetStatus(certificateId, userId).toString
  }

  def getAvailableForUser(userId:Long,
                          filter: CertificateFilter,
                          skipTake: Option[SkipTake],
                          sortAZ: Boolean
                           ): RangeResult[Certificate] = {

    val userCertificateIds = certificateToUserRepository.getByUserId(userId)
      .map(_.certificateId)

    var certificates = certificateRepository.getBy(filter).sortBy(_.title.toLowerCase)

    certificates = certificates.filter(certificate => !userCertificateIds.contains(certificate.id))


    val total = certificates.length

    if (!sortAZ) certificates = certificates.reverse

    for (SkipTake(skip, take) <- skipTake)
      certificates = certificates.slice(skip, skip + take)

    RangeResult(total, certificates)
  }

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          sortAZ: Boolean,
                                          skipTake: Option[SkipTake],
                                          titlePattern: Option[String]
                                           ): RangeResult[Certificate] = {

    var certificates = getCertificatesByUserWithOpenBadges(userId, companyId, titlePattern)
    val total = certificates.length

    if (!sortAZ)
      certificates = certificates.reverse

    for (SkipTake(skip, take) <- skipTake)
      certificates = certificates.slice(skip, skip + take)

    RangeResult(total, certificates)
  }

  def getCertificatesByUserWithOpenBadges(userId: Long, companyId: Long, titlePattern: Option[String]): Seq[Certificate] = {
    val certificates = getSuccessByUser(userId, companyId, titlePattern)

    //Backbone squash models with the same ids to one, so we set different negative ids
    val openBadges = getOpenBadges(userId, companyId, titlePattern).zipWithIndex
      .filter { case (badge, index) => !certificates.exists(c => c.title == badge.title) }
      .map { case (badge, index) => badge.copy(id = -index - 1) }

    certificates ++ openBadges
  }


  def getIssuerBadge(certificateId: Long, liferayUserId: Long, rootUrl: String): BadgeResponse = {
    val recipient = "sha256$" + hashEmail(userLocalServiceHelper.getUser(liferayUserId).getEmailAddress)
    val issueOn = DateTime.now.toString("yyyy-MM-dd")

    val identity = IdentityModel(recipient)
    val badgeUrl = "%s/delegate/certificates/%s/issue_badge/badge?userId=%s&rootUrl=%s".format(
      rootUrl,
      certificateId,
      liferayUserId,
      rootUrl)

    val verificationUrl = "%s/delegate/certificates/%s/issue_badge?userId=%s&rootUrl=%s".format(
      rootUrl,
      certificateId,
      liferayUserId,
      rootUrl)
    val verification = VerificationModel(url = verificationUrl)

    BadgeResponse(certificateId.toString, identity, badgeUrl, verification, issueOn)
  }

  def getBadgeModel(certificateId: Long, rootUrl: String): BadgeModel = {
    val certificate = certificateRepository.getById(certificateId)

    val name = certificate.title.replaceAll("%20", " ")
    val imageUrl = if (certificate.logo == "")
      "%s/learn-portlet/img/certificate-default.jpg".format(rootUrl)
    else
      "%s/delegate/files/images?folderId=%s&file=%s".format(rootUrl, certificate.id, certificate.logo)

    val description = certificate.shortDescription.replaceAll("%20", " ")
    val issuerUrl = "%s/delegate/certificates/%s/issue_badge/issuer?rootUrl=%s".format(
      rootUrl,
      certificateId,
      rootUrl)

    BadgeModel(name, description, imageUrl, rootUrl, issuerUrl)
  }

  def getIssuerModel(rootUrl: String): IssuerModel = {

    val issuerName = settingStorage
      .getByKey(SettingType.IssuerName)
      .getOrElse(model.EmptySetting(SettingType.IssuerName))
      .value

    val issuerUrl = settingStorage.getByKey(SettingType.IssuerURL)
      .getOrElse(model.EmptySetting(SettingType.IssuerURL, rootUrl))
      .value

    val issuerEmail = settingStorage.getByKey(SettingType.IssuerEmail)
      .getOrElse(model.EmptySetting(SettingType.IssuerEmail))
      .value

    IssuerModel(issuerName, issuerUrl, issuerEmail)
  }

  def getUsers(c: Certificate) = {
    certificateToUserRepository
      .getBy(CertificateStateFilter(certificateId = Some(c.id)))
      .map(p => (p.userJoinedDate, UserLocalServiceHelper().getUser(p.userId)))
  }

  private def hashEmail(email: String) = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(email.getBytes)
    HexHelper().toHexString(md.digest())
  }
}
