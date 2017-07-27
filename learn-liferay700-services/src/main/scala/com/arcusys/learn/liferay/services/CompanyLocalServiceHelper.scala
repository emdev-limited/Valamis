package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.LCompany
import com.liferay.portal.kernel.model.Company
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil

import scala.collection.JavaConverters._



object CompanyLocalServiceHelper {
  def getCompanies: List[LCompany] = CompanyLocalServiceUtil.getCompanies.asScala.toList
  def getCompany(id: Long): LCompany = CompanyLocalServiceUtil.getCompany(id)
  def getCompanyGroupId(id: Long): Long = CompanyLocalServiceUtil.getCompany(id).getGroupId
}
