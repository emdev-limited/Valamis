package com.arcusys.valamis.certificate.service

import java.security.MessageDigest

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.certificate.model.badge._
import com.arcusys.valamis.certificate.service.util.OpenBadgesHelper
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.settings.model
import com.arcusys.valamis.settings.model.SettingType
import com.arcusys.valamis.settings.storage.SettingStorage
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.HexHelper
import org.joda.time.DateTime

//TODO: remove hardcoded urls !!!
abstract class CertificateBadgeServiceImpl extends CertificateBadgeService {

  def certificateRepository: CertificateRepository
  def userLocalServiceHelper: UserLocalServiceHelper
  def settingStorage: SettingStorage
  def userService: UserService

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
      "%s/delegate/files/resources?file=/img/certificate_cover.svg".format(rootUrl)
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

  def getOpenBadges(userId: Long,
                    companyId: Long,
                    titlePattern: Option[String]): List[Certificate] = {
    val userEmail = userService.getById(userId).getEmailAddress

    var openbadges = OpenBadgesHelper.getOpenBadges(userEmail).map(x =>
      Certificate(id = -1,
        title = x("title").toString,
        description = x("description").toString,
        logo = x("logo").toString,
        companyId = companyId,
        createdAt = DateTime.now)
    )

    if (titlePattern.isDefined) {
      val title = titlePattern.get.toLowerCase
      openbadges = openbadges.filter(_.title.toLowerCase contains title)
    }

    openbadges
      .sortBy(_.title.toLowerCase)
  }

  private def hashEmail(email: String) = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(email.getBytes)
    HexHelper().toHexString(md.digest())
  }
}
