package com.arcusys.valamis.web.service

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.services.{CompanyHelper, ShardUtilHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper

object LiferayContext {
  val log = LogFactoryHelper.getLog(this.getClass)

  def init(companyId: Long): Unit = {
    ShardUtilHelper.initShardUtil(CompanyHelper.getCompanyId)
  }

  def init(request: HttpServletRequest): Unit = {
    val companyId = PortalUtilHelper.getCompanyId(request)

    init(companyId)
  }

  def init(): Unit = {
    val companyId = CompanyHelper.getCompanyId

    if (companyId > 0) init(companyId)
    else log.warn("no company id in current thread")
  }
}
