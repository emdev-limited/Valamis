package com.arcusys.valamis.certificate.service



import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.{PortletName, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.kernel.util.PrefsPropsUtil


abstract class CertificateNotificationServiceImpl extends CertificateNotificationService {

  private val log: Log = LogFactoryUtil.getLog(this.getClass)
  def certificateRepository: CertificateRepository

  def certificateService: CertificateService

  def sendAchievedNotification(state: CertificateState): Unit = {

    try {
      val certificate = certificateRepository.getById(state.certificateId)

      val company = CompanyLocalServiceHelper.getCompany(certificate.companyId)
      val user = UserLocalServiceHelper().getUser(state.userId)

      val link = certificateService.getCertificatePdfUrl(certificate.companyId,
        state.userId,
        certificate.id,
        company.getGroupId)

      val pdfLink = getLink(link, certificate.title)

      if (state.status == CertificateStatuses.Success) {

        if (PrefsPropsUtil.getBoolean(certificate.companyId, "valamis.certificate.user.achieved.enable")) {
          EmailNotificationHelper.sendNotification(certificate.companyId,
            state.userId,
            "valamisCertificateUserAchievedBody",
            "valamisCertificateUserAchievedSubject",
            Map(
              "[$CERTIFICATE_NAME$]" -> certificate.title,
              "[$CERTIFICATE_PRINT_LINK$]" -> pdfLink,
              "[$USER_SCREENNAME$]" -> user.getFullName,
              "[$PORTAL_URL$]" -> company.getVirtualHostname
            )
          )
        }

        prepareSendNotification(state.userId,
          "achieved",
          certificate.title,
          certificateService.getCertificateURL(certificate))
      }
    } catch {
      case e: Exception => log.error(e)
    }
  }

  def sendUserAddedNotification(isCurrentUser: Boolean,
                                certificate: Certificate,
                                userId: Long): Unit = {
    try {
      if (!isCurrentUser && certificate.isActive) {
        UserLocalServiceHelper().fetchUser(userId).foreach { user =>
          val link = certificateService.getCertificateURL(certificate)
          if (PrefsPropsUtil.getBoolean(certificate.companyId, "valamis.certificate.user.added.enable")) {
            val company = CompanyLocalServiceHelper.getCompany(certificate.companyId)
            val linkHtml = getLink(link, certificate.title)
            EmailNotificationHelper.sendNotification(certificate.companyId,
              userId,
              "valamisCertificateUserAddedBody",
              "valamisCertificateUserAddedSubject",
              Map(
                "[$CERTIFICATE_NAME$]" -> certificate.title,
                "[$CERTIFICATE_LINK$]" -> linkHtml,
                "[$USER_SCREENNAME$]" -> user.getFullName,
                "[$PORTAL_URL$]" -> company.getVirtualHostname

              )
            )
          }
          prepareSendNotification(userId,
            "added",
            certificate.title,
            link)
        }
      }
    } catch {
      case e: Exception => log.error(e)
    }
  }

  def sendCertificateDeactivated(certificate: Certificate, userId: Long): Unit = {
    try {
      UserLocalServiceHelper().fetchUser(userId).foreach { user =>
        if (PrefsPropsUtil.getBoolean(certificate.companyId, "valamis.certificate.user.deactivated.enable")) {
          val company = CompanyLocalServiceHelper.getCompany(certificate.companyId)
          EmailNotificationHelper.sendNotification(certificate.companyId,
            userId,
            "valamisCertificateUserDeactivatedBody",
            "valamisCertificateUserDeactivatedSubject",
            Map(
              "[$CERTIFICATE_NAME$]" -> certificate.title,
              "[$USER_SCREENNAME$]" -> user.getFullName,
              "[$PORTAL_URL$]" -> company.getVirtualHostname
            )
          )
        }
        prepareSendNotification(userId,
          "deactivated",
          certificate.title,
          certificateService.getCertificateURL(certificate))
      }
    } catch {
      case e: Exception => log.error(e)
    }
  }

  def sendCertificateExpires(certificate: Certificate, userId: Long, days: Long): Unit = {
    try {
      UserLocalServiceHelper().fetchUser(userId).foreach { user =>
        val link = certificateService.getCertificateURL(certificate)
        if (PrefsPropsUtil.getBoolean(certificate.companyId, "valamis.certificate.expires.enable")) {
          val linkHtml = getLink(link, certificate.title)
          val company = CompanyLocalServiceHelper.getCompany(certificate.companyId)
          EmailNotificationHelper.sendNotification(certificate.companyId,
            userId,
            "valamisCertificateExpiresBody",
            "valamisCertificateExpiresSubject",
            Map(
              "[$CERTIFICATE_LINK$]" -> linkHtml,
              "[$DAYS$]" -> days.toString,
              "[$USER_SCREENNAME$]" -> user.getFullName,
              "[$PORTAL_URL$]" -> company.getVirtualHostname
            )
          )
        }
        prepareSendNotification(userId,
          "expires",
          certificate.title,
          link)
      }
    } catch {
      case e: Exception => log.error(e)
    }
  }


  def sendCertificateExpired(certificate: Certificate, userId: Long, days: Long): Unit = {
    try {
      UserLocalServiceHelper().fetchUser(userId).foreach { user =>
        val link = certificateService.getCertificateURL(certificate)
        if (PrefsPropsUtil.getBoolean(certificate.companyId, "valamis.certificate.expired.enable")) {
          val linkHtml = getLink(link, certificate.title)
          val company = CompanyLocalServiceHelper.getCompany(certificate.companyId)
          EmailNotificationHelper.sendNotification(certificate.companyId,
            userId,
            "valamisCertificateExpiredBody",
            "valamisCertificateExpiredSubject",
            Map(
              "[$CERTIFICATE_LINK$]" -> linkHtml,
              "[$DAYS$]" -> days.toString,
              "[$USER_SCREENNAME$]" -> user.getFullName,
              "[$PORTAL_URL$]" -> company.getVirtualHostname
            )
          )
        }
        prepareSendNotification(userId,
          "expired",
          certificate.title,
          link)
      }
    } catch {
      case e: Exception => log.error(e)
    }
  }


  private def prepareSendNotification(userId: Long, messageType: String, title: String, link: String): Unit = {
    val model = CertificateNotificationModel(messageType, title, link, userId)
    UserNotificationEventLocalServiceHelper.sendNotification(JsonHelper.toJson(model),
      userId,
      PortletName.CertificateManager.key)
  }

  private def getLink(link: String, name: String): String = "<a href=\"" + link + "\">" + name + "</a>"
}
