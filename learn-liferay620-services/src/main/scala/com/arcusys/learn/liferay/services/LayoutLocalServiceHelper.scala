package com.arcusys.learn.liferay.services

import com.liferay.portal.NoSuchLayoutSetPrototypeException
import com.liferay.portal.model.{Group, Layout}
import com.liferay.portal.service.{LayoutLocalServiceUtil, ServiceContext}
import java.io.{File, InputStream}

import com.arcusys.learn.liferay.LiferayClasses.LLayout
import com.liferay.portal.util.LayoutTypePortletFactoryUtil
import com.liferay.portal.kernel.log.LogFactoryUtil

import scala.collection.JavaConverters._

object LayoutLocalServiceHelper {
  val logger = LogFactoryUtil.getLog(getClass)
  def getLayouts(groupId: Long, privateLayout: Boolean): Seq[LLayout] =
    LayoutLocalServiceUtil.getLayouts(groupId, privateLayout).asScala

  def getLayouts(groupId: Long, privateLayout: Boolean, layoutType: String): java.util.List[Layout] =
    LayoutLocalServiceUtil.getLayouts(groupId, privateLayout, layoutType)

  def getLayout(plid: Long): LLayout = LayoutLocalServiceUtil.getLayout(plid)

  def getFriendlyURLLayout(groupId: Long,
    privateLayout: Boolean,
    friendlyURL: String): Layout =
    LayoutLocalServiceUtil.getFriendlyURLLayout(groupId, privateLayout, friendlyURL)

  def getLayoutsCount(group: Group, privateLayout: Boolean): Int =
    LayoutLocalServiceUtil.getLayoutsCount(group, privateLayout)

  def deleteLayouts(groupId: Long,
    privateLayout: Boolean,
    serviceContext: ServiceContext) {
    LayoutLocalServiceUtil.deleteLayouts(groupId, privateLayout, serviceContext)
  }

  def importLayouts(userId: Long,
    groupId: Long,
    privateLayout: Boolean,
    parameterMap: java.util.Map[String, Array[String]],
    file: File) {
    LayoutLocalServiceUtil.importLayouts(userId, groupId, privateLayout, parameterMap, file)
  }

  def addLayout(userId: Long,
    groupId: Long,
    privateLayout: Boolean,
    parentLayoutId: Long,
    name: String,
    title: String,
    description: String,
    layoutType: String,
    hidden: Boolean,
    friendlyURL: String,
    serviceContext: ServiceContext): Layout = {
    LayoutLocalServiceUtil.addLayout(userId, groupId, privateLayout, parentLayoutId, name, title, description, layoutType,
      hidden, friendlyURL, serviceContext)
  }

  def updateLayout(groupId: Long,
    privateLayout: Boolean,
    layoutId: Long,
    typeSettings: String): Layout =
    LayoutLocalServiceUtil.updateLayout(groupId, privateLayout, layoutId, typeSettings)

  def importPortletInfo(userId: Long,
    plid: Long,
    groupId: Long,
    portletId: String,
    parameterMap: java.util.Map[String, Array[String]],
    is: InputStream) {
    LayoutLocalServiceUtil.importPortletInfo(userId, plid, groupId, portletId, parameterMap, is)
  }

 def getPortletIds(layout: LLayout) = {
    try {
      LayoutTypePortletFactoryUtil.create(layout).getPortletIds.asScala
    }
    catch {
      // No LayoutSetPrototype exists with the key
      case e: NoSuchLayoutSetPrototypeException =>
        logger.warn(e.getMessage)
        Seq()
    }
  }
}
