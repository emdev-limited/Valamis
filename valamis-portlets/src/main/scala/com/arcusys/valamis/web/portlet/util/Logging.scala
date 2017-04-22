package com.arcusys.valamis.web.portlet.util

import org.slf4j.LoggerFactory

trait Logging {
  val logger = LoggerFactory.getLogger(this.getClass.getName)
}


