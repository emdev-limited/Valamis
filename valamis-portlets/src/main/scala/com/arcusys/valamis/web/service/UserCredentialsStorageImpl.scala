package com.arcusys.valamis.web.service

import javax.portlet.{PortletRequest, PortletSession}

import com.arcusys.learn.liferay.services.ServiceContextHelper
import com.arcusys.valamis.lrs.model.EndpointInfo
import com.arcusys.valamis.lrs.service.UserCredentialsStorage

class UserCredentialsStorageImpl extends UserCredentialsStorage {
  val LrsEndpointInfo = "LRS_ENDPOINT_INFO"

  def get: Option[EndpointInfo] = {
    Option(ServiceContextHelper.getServiceContext).flatMap { context =>

      val request = context.getRequest
      val session = request.getSession

      session.getAttribute(LrsEndpointInfo) match {
        case e: EndpointInfo => Some(e)
        case _ => None
      }
    }
  }

  def get(request: PortletRequest): Option[EndpointInfo] = {
    request.getPortletSession.getAttribute(LrsEndpointInfo, PortletSession.APPLICATION_SCOPE) match {
      case e: EndpointInfo => Some(e)
      case _ => None
    }
  }

  def set(e: EndpointInfo, request: PortletRequest): Unit = {
    request.getPortletSession.setAttribute(LrsEndpointInfo, e, PortletSession.APPLICATION_SCOPE)
  }
}
