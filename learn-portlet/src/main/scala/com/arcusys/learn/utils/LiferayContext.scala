package com.arcusys.learn.utils

import javax.servlet.http.HttpServletRequest

import com.liferay.portal.kernel.dao.shard.ShardUtil
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.model.Company
import com.liferay.portal.security.auth.CompanyThreadLocal
import com.liferay.portal.service.ShardLocalServiceUtil
import com.liferay.portal.util.PortalUtil

object LiferayContext {
  val log = LogFactoryUtil.getLog(this.getClass)

  def init(companyId: Long): Unit = {
    if (ShardUtil.isEnabled) {
      CompanyThreadLocal.setCompanyId(companyId)

      val shard = ShardLocalServiceUtil.getShard(classOf[Company].getName, companyId)

      ShardUtil.setTargetSource(shard.getName)
    }
  }

  def init(request: HttpServletRequest): Unit = {
    val companyId = PortalUtil.getCompanyId(request)

    init(companyId)
  }

  def init(): Unit = {
    val companyId = CompanyThreadLocal.getCompanyId

    if (companyId > 0) init(companyId)
    else log.warn("no company id in current thread")
  }
}
