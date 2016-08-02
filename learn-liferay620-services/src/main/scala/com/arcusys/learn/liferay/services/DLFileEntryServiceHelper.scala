package com.arcusys.learn.liferay.services

import com.liferay.portlet.documentlibrary.service.DLFileEntryServiceUtil

object DLFileEntryServiceHelper {
  def getFileAsStream(entryId: Long, version: String) = DLFileEntryServiceUtil.getFileAsStream(entryId, version)
}
