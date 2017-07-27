package com.arcusys.valamis.web.servlet.public.model.request

import com.arcusys.valamis.web.servlet.public.model.response.RerunLimits
import com.arcusys.valamis.web.servlet.public.model.response.VisibilityType.VisibilityType
import org.joda.time.DateTime

/**
  * Created by pkornilov on 2/9/17.
  */
//TODO VALAMIS_API move it to lesson models and make use of it in LessonService
case class LessonUpdateRequest(
  title: String,
  description: String,
  visibility: VisibilityType,
  beginDate: Option[DateTime],
  endDate: Option[DateTime],
  requiredReview: Boolean,
  scoreLimit: Double,
  rerunLimits: Option[RerunLimits]
)

