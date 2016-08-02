package com.arcusys.learn.liferay.services

import com.liferay.mail.service.MailServiceUtil
import com.liferay.portal.kernel.mail.MailMessage

object MailServiceHelper {

  def sendEmail(m: MailMessage) = MailServiceUtil.sendEmail(m)
}
