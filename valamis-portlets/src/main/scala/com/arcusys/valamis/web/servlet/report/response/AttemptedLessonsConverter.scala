package com.arcusys.valamis.web.servlet.report.response

import com.arcusys.valamis.reports.model.AttemptedLessonsRow

/**
  * Created by amikhailov on 23.11.16.
  */
object AttemptedLessonsConverter {
  def toResponse(report: Seq[AttemptedLessonsRow]): Seq[AttemptedLessonsResponse] = {
    report map { item =>
      AttemptedLessonsResponse(item.name, Seq(
        StateResponse(
          "attempted",
          item.countAttempted
        ),
        StateResponse(
          "completed",
          item.countFinished
        )
      ))
    }
  }
}