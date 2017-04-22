package com.arcusys.learn.liferay.model

import com.liferay.portal.model.Group

object LGroup {
  type LFGroup = Group
  def getGroupClass: Class[Group] = classOf[Group]
}

/**
  * Created by mminin on 31/08/16.
  */
class LGroup(private[liferay] val group: Group) {
  def getGroupId = group.getGroupId

  def getName = group.getName

  def setName(v: String) = group.setName(v)


  def getDescription = group.getDescription

  def setDescription(v: String) = group.setDescription(v)


  def isActive = group.isActive

  def setActive(v: Boolean) = group.setActive(v)


  def getFriendlyURL = group.getFriendlyURL

  def setFriendlyURL(v: String) = group.setFriendlyURL(v)


  def getType = group.getType

  def setType(v: Int) = group.setType(v)


  def getCompanyId = group.getCompanyId

  def isUserPersonalSite = group.isUserPersonalSite

  def isUser = group.isUser

  def getDescriptiveName = group.getDescriptiveName

  def getOrganizationId = group.getOrganizationId

  def isSite = group.isSite

  def getClassPK = group.getClassPK

  def getPrivateLayoutsPageCount = group.getPrivateLayoutsPageCount

  def getPublicLayoutsPageCount = group.getPublicLayoutsPageCount

}
