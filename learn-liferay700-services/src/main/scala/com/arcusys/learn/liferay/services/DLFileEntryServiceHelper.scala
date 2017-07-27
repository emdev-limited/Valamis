package com.arcusys.learn.liferay.services

import com.liferay.document.library.kernel.service.DLFileEntryServiceUtil

object DLFileEntryServiceHelper {
  def getFileAsStream(entryId: Long, version: String) = DLFileEntryServiceUtil.getFileAsStream(entryId, version)
}
