package com.arcusys.valamis.persistence.impl.scorm.model

case class AttemptDataModel(id: Option[Long],
                                dataKey: String,
                                dataValue: Option[String],
                                attemptId: Long,
                                activityId: String)

