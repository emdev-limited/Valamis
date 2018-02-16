package com.arcusys.valamis.export.model

case class ExportModel(
  guid: String,
  isFinished: Boolean,
  data: String = "",
  linkCSV: String = "",
  linkJSON: String = "")