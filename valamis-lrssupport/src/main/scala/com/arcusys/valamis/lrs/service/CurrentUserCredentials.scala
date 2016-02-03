package com.arcusys.valamis.lrs.service

import javax.portlet.PortletSession
import javax.servlet.http.HttpSession

import com.arcusys.valamis.lrs.model.EndpointInfo

/**
 * Created by mminin on 29.06.15.
 */
class CurrentUserCredentials {
  val LrsEndpointInfo = "LRS_ENDPOINT_INFO"

  def get(session: HttpSession): Option[EndpointInfo] = {
    session.getAttribute(LrsEndpointInfo) match {
      case e: EndpointInfo => Some(e)
      case _ => None
    }
  }

  def get(session: PortletSession): Option[EndpointInfo] = {
    session.getAttribute(LrsEndpointInfo, PortletSession.APPLICATION_SCOPE) match {
      case e: EndpointInfo => Some(e)
      case _ => None
    }
  }

  def set(e: EndpointInfo, session: PortletSession): Unit = {
    session.setAttribute(LrsEndpointInfo, e, PortletSession.APPLICATION_SCOPE)
  }
}
