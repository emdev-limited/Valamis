package com.arcusys.learn.liferay

import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}

object LogFactoryHelper {
  def getLog(c: Class[_]): Log = LogFactoryUtil.getLog(c)
}
