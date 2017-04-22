package com.arcusys.learn.liferay.services

import com.liferay.portal.security.auth.CompanyThreadLocal

object CompanyHelper {
  def setCompanyId(companyId: Long): Unit = CompanyThreadLocal.setCompanyId(companyId)

  def getCompanyId: Long = CompanyThreadLocal.getCompanyId

}
