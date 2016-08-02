package com.arcusys.valamis.persistence.impl.scorm.model

case class ActivityDataMapModel(id: Option[Long],
                                packageId: Option[Long],
                                activityId: Option[String],
                                targetId: String,
                                readSharedData: Boolean,
                                writeSharedData: Boolean)

