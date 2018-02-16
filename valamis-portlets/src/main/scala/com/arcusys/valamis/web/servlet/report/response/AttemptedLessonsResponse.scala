package com.arcusys.valamis.web.servlet.report.response

/**
  * Created by amikhailov on 31/05/2017.
  */
case class AttemptedLessonsResponse(name: String, categories: Seq[StateResponse])

case class StateResponse(name: String, value: Integer)