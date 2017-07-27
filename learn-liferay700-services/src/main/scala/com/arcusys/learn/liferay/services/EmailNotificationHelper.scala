package com.arcusys.learn.liferay.services

import javax.mail.internet.InternetAddress

import com.arcusys.learn.liferay.LiferayClasses.{LMailMessage, LUser}
import com.liferay.mail.kernel.service.MailServiceUtil
import com.liferay.portal.kernel.util._

object EmailNotificationHelper  {

  def sendNotification(companyId: Long,
                       userId: Long,
                       bodyPreferencesName: String,
                       subjectPreferencesName: String,
                       data: Map[String, String]): Unit = {

    UserLocalServiceHelper().fetchUser(userId).foreach { user =>
      val template = getTemplate(companyId, bodyPreferencesName, subjectPreferencesName, user)


      val currentBody = StringUtil.replace(template._2, data.keys.toArray, data.values.toArray)

      sendEmailNotification(companyId, user, template._1, currentBody)
    }
  }

  private def getTemplate(companyId: Long,
                          bodyPreferencesName: String,
                          subjectPreferencesName: String,
                          user: LUser): (String, String) = {
    val companyPortletPreferences = PrefsPropsUtil.getPreferences(companyId, true)
    val body = LocalizationUtil.getPreferencesValue(companyPortletPreferences,
      bodyPreferencesName,
      user.getLanguageId)
    val subject = LocalizationUtil.getPreferencesValue(companyPortletPreferences,
      subjectPreferencesName,
      user.getLanguageId)
    (subject, body)
  }

  private def sendEmailNotification(companyId: Long,
                                    user: LUser,
                                    subject: String,
                                    body: String) = {

    val adminEmail = PrefsPropsUtil.getString(companyId, PropsKeys.ADMIN_EMAIL_FROM_ADDRESS)
    val userEmail = user.getEmailAddress
    val mail = new LMailMessage

    mail.setTo(new InternetAddress(userEmail))
    mail.setFrom(new InternetAddress(adminEmail))
    mail.setSubject(subject)
    mail.setBody(body)
    mail.setHTMLFormat(true)


    MailServiceUtil.sendEmail(mail)
  }
}
