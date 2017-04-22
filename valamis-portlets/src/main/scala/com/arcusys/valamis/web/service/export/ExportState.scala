package com.arcusys.valamis.web.service.export

case class ExportState(isFinished: Boolean,
                       data: String,
                       linkCSV: String = "",
                       linkJSON: String = "",
                       isCancelled: Boolean = false)
