package com.arcusys.valamis.hook

import javax.portlet.PortletPreferences

import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.kernel.util.PrefsPropsUtil

class AddNotificationTemplate extends SimpleAction {

  private val log = LogFactoryUtil.getLog(classOf[AddNotificationTemplate])

  override def run(companyIds: Array[String]): Unit = {
    log.info("Upgrade valamis notification email template")
    companyIds.foreach(companyId => {
      SetValamisEmailTemplates(companyId)
    })
  }


  private def SetValamisEmailTemplates(companyId: String) {
    val valamisCertificateAchivedSubject: String = "valamisCertificateUserAchievedSubject"
    val valamisCertificateAchievedBody: String = "valamisCertificateUserAchievedBody"

    val valamisCertificateAddedSubject: String = "valamisCertificateUserAddedSubject"
    val valamisCertificateAddedBody: String = "valamisCertificateUserAddedBody"

    val valamisCertificateDeactivatedSubject: String = "valamisCertificateUserDeactivatedSubject"
    val valamisCertificateDeactivatedBody: String = "valamisCertificateUserDeactivatedBody"

    val valamisCourseAddedSubject: String = "valamisCourseUserAddedSubject"
    val valamisCourseAddedBody: String = "valamisCourseUserAddedBody"

    val valamisCourseLessonAvailableSubject: String = "valamisCourseLessonAvailableSubject"
    val valamisCourseLessonAvailableBody: String = "valamisCourseLessonAvailableBody"

    val valamisGradeCourseSubject: String = "valamisGradeCourseSubject"
    val valamisGradeCourseBody: String = "valamisGradeCourseBody"

    val valamisGradeLessonSubject: String = "valamisGradeLessonSubject"
    val valamisGradeLessonBody: String = "valamisGradeLessonBody"

    val valamisCertificateExpiresSubject: String = "valamisCertificateExpiresSubject"
    val valamisCertificateExpiresBody: String = "valamisCertificateExpiresBody"

    val valamisCertificateExpiredSubject: String = "valamisCertificateExpiredSubject"
    val valamisCertificateExpiredBody: String = "valamisCertificateExpiredBody"


    val preferences = PrefsPropsUtil.getPreferences(companyId.toLong)

    val header =
      """<font color=#2C2C2C>
        |  <b>Dear [$USER_SCREENNAME$] </b>
        |</font>
        |<br />
        |<br />""".stripMargin

    val footer =
      """
        |  <hr color="#D8D8D8"/>
        |<table style="width:100%; font-size:12px">
        |  <tr>
        |    <td width="50%" align="left">[$PORTAL_URL$]</td>
        |    <td width="50%" align="right">Created in Valamis
        |    </td>
        |  </tr>
        |</table>""".stripMargin

    setValueIfEmpty(preferences, valamisCertificateAchivedSubject, "You've achieved a certificate!")
    setValueIfEmpty(preferences,
      valamisCertificateAchievedBody,
      header +
        "You have achieved [$CERTIFICATE_PRINT_LINK$] certificate. You can print your certificate. <br/><br/>" +
        "Please visit the link above to access the certificate." +
        footer)

    setValueIfEmpty(preferences, valamisCertificateAddedSubject, "You've been added to a certificate")
    setValueIfEmpty(preferences,
      valamisCertificateAddedBody,
      header +
        "You've been added to [$CERTIFICATE_LINK$] certificate.<br/><br/>" +
        "Please visit the link above to see the certificate." +
        footer)

    setValueIfEmpty(preferences, valamisCertificateDeactivatedSubject, "A certificate has been deactivated")
    setValueIfEmpty(preferences,
      valamisCertificateDeactivatedBody,
      header +
        "[$CERTIFICATE_NAME$] certificate that you are a member of, has been deactivated. <br/>" +
        "Deactivated certificates cannot be achieved before they are activated again by admins." +
        footer)

    setValueIfEmpty(preferences, valamisCourseAddedSubject, "You've been added to a course")
    setValueIfEmpty(preferences,
      valamisCourseAddedBody,
      header +
        "You've been added to course [$COURSE_LINK$].<br/><br/>" +
        "Please visit the link above to access the course." +
        footer)

    setValueIfEmpty(preferences, valamisCourseLessonAvailableSubject, "New lessons available")
    setValueIfEmpty(preferences,
      valamisCourseLessonAvailableBody,
      header +
        "There is a new lesson [$LESSON_LINK$] available on your course [$COURSE_LINK$]. <br><br>" +
        "Please visit the link above to access the lessons." +
        footer)

    setValueIfEmpty(preferences, valamisGradeCourseSubject, "You've got a new grade for course")
    setValueIfEmpty(preferences,
      valamisGradeCourseBody,
      header +
        "You've got a new grade for [$COURSE_LINK$] course: [$GRADE$]." +
        footer)

    setValueIfEmpty(preferences, valamisGradeLessonSubject, "You've got a new grade for lesson")
    setValueIfEmpty(preferences,
      valamisGradeLessonBody,
      header +
        "You've got a new grade for [$LESSON_LINK$] lesson: [$GRADE$]." +
        footer)

    setValueIfEmpty(preferences, valamisCertificateExpiresSubject, "Your certificate expires")
    setValueIfEmpty(preferences,
      valamisCertificateExpiresBody,
      header +
        "Certificate [$CERTIFICATE_LINK$] expires in [$DAYS$] days.<br><br>" +
        " Please visit the link above to see the certificate." +
        footer)

    setValueIfEmpty(preferences, valamisCertificateExpiredSubject, "Your certificate has expired")
    setValueIfEmpty(preferences,
      valamisCertificateExpiredBody,
      header +
        "Certificate [$CERTIFICATE_LINK$] has expired. <br><br> " +
        "Please visit the link above to see the certificate." +
        footer)

    preferences.store()
  }

  private def setValueIfEmpty(preferences: PortletPreferences, key: String, value: String): Unit = {
    if ("".equals(preferences.getValue(key, "")))
      preferences.setValue(key, value)
  }
}
