package com.arcusys.learn.liferay.util

import javax.portlet.{ActionRequest, PortletRequest, RenderRequest}

import com.liferay.portal.security.auth.CompanyThreadLocal
import com.liferay.portal.service.{CompanyLocalServiceUtil, ServiceContextThreadLocal}
import com.liferay.portal.util.PortalUtil
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.liferay.portal.kernel.upload.UploadPortletRequest

object PortalUtilHelper {
  def getOriginalServletRequest(req: HttpServletRequest) = PortalUtil.getOriginalServletRequest(req)

  def getPortalURL(req: RenderRequest): String = PortalUtil.getPortalURL(req)

  def getPortletId(request: RenderRequest) = PortalUtil.getPortletId(request)

  def getClassNameId(className: String): Long = PortalUtil.getClassNameId(className)

  def getBasicAuthUserId(request: HttpServletRequest): Long = PortalUtil.getBasicAuthUserId(request)

  def getUser(request: HttpServletRequest): LUser = PortalUtil.getUser(request)

  def getUserId(request: HttpServletRequest): Long = PortalUtil.getUserId(request)

  def getUploadPortletRequest(request: ActionRequest): UploadPortletRequest = PortalUtil.getUploadPortletRequest(request)

  def getPortalURL(req: HttpServletRequest): String = PortalUtil.getPortalURL(req)

  def getPortalURL(virtualHost: String, port: Int, isSecure: Boolean): String = PortalUtil.getPortalURL(virtualHost, port, isSecure)

  def getPortalPort(isSecure: Boolean): Int = PortalUtil.getPortalPort(isSecure)

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

  def getPathContext: String = {
    PortalUtil.getPathContext
  }

  def getServletPathContext(request: PortletRequest): String = {
    PortalUtil.getPathContext(request)
  }
}
