package com.arcusys.valamis.persistence.impl.scorm.model

case class ActivityStateTreeModel(id: Option[Long],
                                  currentActivityId: Option[String],
                                  suspendedActivityId: Option[String],
                                  attemptId: Option[Long])