package com.arcusys.valamis.log

import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}

/**
  * Created by mminin on 24.12.15.
  */
trait LogSupport {
  protected val log: Log = LogFactoryUtil.getLog(this.getClass)
}
