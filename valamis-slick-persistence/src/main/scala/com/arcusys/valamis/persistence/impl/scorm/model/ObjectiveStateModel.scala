package com.arcusys.valamis.persistence.impl.scorm.model

case class ObjectiveStateModel(id: Option[Long],
                                satisfied: Option[Boolean],
                                normalizedMeasure : Option[BigDecimal],
                                mapKey: Option[String],
                                activityStateId: Long,
                                objectiveId: Option[Long])


