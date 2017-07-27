package com.arcusys.valamis.web.listener

import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.reports.service.ReportService
import com.arcusys.valamis.web.configuration.InjectableSupport
import com.liferay.portal.kernel.messaging.{Message, MessageListener}

/**
  * Created by amikhailov on 26.01.17.
  */
class ReportScheduler extends MessageListener
  with InjectableSupport
  with LogSupport {

  lazy val reportService = inject[ReportService]
  val ONE_DAY = 3600 * 24

  override def receive(message: Message): Unit = {
    try {
      reportService.cleanReportDir(ONE_DAY)
    } catch {
      case e: Throwable => log.error(e.getMessage, e)
    }
  }
}