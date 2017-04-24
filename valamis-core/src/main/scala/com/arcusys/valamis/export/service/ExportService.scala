package com.arcusys.valamis.export.service

import com.arcusys.valamis.export.model.ExportModel

/**
  * Export statements to json and CSV
  */
trait ExportService {
  def export(count: Option[Long],
             actor: Option[String],
             activity: Option[String],
             verb: Option[String],
             format: Option[String],
             since: Option[String],
             until: Option[String],
             registration: Option[String],
             relatedAgents: Option[String],
             relatedActivities: Option[String]): String

  def cancel(guid: String): Unit

  def getStatus(guid: String): ExportModel
}
