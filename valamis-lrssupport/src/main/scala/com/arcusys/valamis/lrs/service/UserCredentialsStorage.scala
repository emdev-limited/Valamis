package com.arcusys.valamis.lrs.service

import javax.portlet.{PortletRequest, PortletSession}

import com.arcusys.valamis.lrs.model.EndpointInfo

trait UserCredentialsStorage {
  def get: Option[EndpointInfo]

  def get(request: PortletRequest): Option[EndpointInfo]

  def set(e: EndpointInfo, request: PortletRequest): Unit
}


