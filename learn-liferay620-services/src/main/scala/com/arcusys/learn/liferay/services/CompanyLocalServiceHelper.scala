package com.arcusys.learn.liferay.services

import com.liferay.portal.model.Company
import com.liferay.portal.service.CompanyLocalServiceUtil
import scala.collection.JavaConverters._

object CompanyLocalServiceHelper {
  def getCompanies: List[Company] = CompanyLocalServiceUtil.getCompanies.asScala.toList
  def getCompany(id: Long): Company = CompanyLocalServiceUtil.getCompany(id)
  def getCompanyGroupId(id: Long): Long = CompanyLocalServiceUtil.getCompany(id).getGroupId
}
