package com.arcusys.learn.liferay.services

import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil

object ClassNameLocalServiceHelper {
  def getClassNameId(value: String): Long = ClassNameLocalServiceUtil.getClassNameId(value)
}
