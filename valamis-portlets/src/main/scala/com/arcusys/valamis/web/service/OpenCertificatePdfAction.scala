package com.arcusys.valamis.web.service

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.{StringPoolHelper, WebKeysHelper}
import com.arcusys.learn.liferay.services.LayoutLocalServiceHelper
import com.arcusys.learn.liferay.util.{ParamUtilHelper, PortalUtilHelper}

class OpenCertificatePdfAction extends LBaseStrutsAction {

  override def execute(originalStrutsAction: LStrutsAction, request: HttpServletRequest, response: HttpServletResponse): String = {

    val userId = ParamUtilHelper.getLong(request, "userId")
    val certificateId = ParamUtilHelper.getLong(request, "certificateId")
    val courseId = ParamUtilHelper.getLong(request, "courseId")
    val companyId = ParamUtilHelper.getLong(request, "companyId")
    val url = getCertificatePdfUrl(companyId, userId, certificateId, courseId)
    response.sendRedirect(url)
    ""
  }

  protected def isValidPlid(plid: Long): Boolean = {
    try {
      LayoutLocalServiceHelper.getLayout(plid)
      true
    } catch {
      case e: LNoSuchLayoutException => false
    }
  }

  private def getCertificatePdfUrl(companyId: Long, userId: Long, certificateId: Long, courseId: Long): String = {
    val sb: StringBuilder = new StringBuilder()
    sb.append(PortalUtilHelper.getLocalHostUrlForCompany(companyId))
    sb.append("/delegate/transcript/user/")
    sb.append(userId)
    sb.append("/certificate/")
    sb.append(certificateId)
    sb.append("/printCertificate")
    sb.append(StringPoolHelper.QUESTION)
    sb.append("courseId")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(courseId)

    sb.toString
  }
}
