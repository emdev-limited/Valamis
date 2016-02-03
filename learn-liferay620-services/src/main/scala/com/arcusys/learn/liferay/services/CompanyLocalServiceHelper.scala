package com.arcusys.learn.liferay.services

import com.liferay.portal.model.Company
import com.liferay.portal.service.CompanyLocalServiceUtil

object CompanyLocalServiceHelper {
  def getCompanies: java.util.List[Company] = CompanyLocalServiceUtil.getCompanies
  def getCompany(id: Long): Company = CompanyLocalServiceUtil.getCompany(id)
  def getCompanyGroupId(id: Long): Long = CompanyLocalServiceUtil.getCompany(id).getGroupId
}
