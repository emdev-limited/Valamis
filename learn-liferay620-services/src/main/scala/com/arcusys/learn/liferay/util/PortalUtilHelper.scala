package com.arcusys.learn.liferay.util

import javax.portlet.PortletRequest
import com.liferay.portal.security.auth.CompanyThreadLocal
import com.liferay.portal.service.{ServiceContextThreadLocal, CompanyLocalServiceUtil}
import com.liferay.portal.util.PortalUtil
import javax.servlet.http.HttpServletRequest
import com.liferay.portal.kernel.util.GetterUtil

object PortalUtilHelper {
  def getCompanyId(portletRequest: PortletRequest): Long =
    PortalUtil.getCompanyId(portletRequest)

  def getCompanyId(portletRequest: HttpServletRequest): Long =
    PortalUtil.getCompanyId(portletRequest)

  def getPathMain: String = PortalUtil.getPathMain

  def getHttpServletRequest(portletRequest: PortletRequest): HttpServletRequest =
    PortalUtil.getHttpServletRequest(portletRequest)

  def getDefaultCompanyId: Long = PortalUtil.getDefaultCompanyId

  def getLocalHostUrl: String = {
    val companyId = CompanyThreadLocal.getCompanyId

    val request = Option(ServiceContextThreadLocal.getServiceContext)
      .flatMap(s => Option(s.getRequest))

    request match {
      case Some(r) => getLocalHostUrl(companyId, r.isSecure)
      case None => getLocalHostUrl(companyId)
    }
  }

  def getLocalHostUrl(companyId: Long, isSecure : Boolean = false): String = {
    lazy val company = CompanyLocalServiceUtil.getCompany(companyId)

    val hostName = company.getVirtualHostname
    val port = PortalUtil.getPortalPort(isSecure)

    PortalUtil.getPortalURL(hostName, port, isSecure) + PortalUtil.getPathContext
  }

  def getHostName(companyId: Long): String =
    "http://" +CompanyLocalServiceUtil.getCompany(companyId).getVirtualHostname


  def getPathContext(request: PortletRequest): String = {
    PortalUtil.getPathContext(request)
  }
}
