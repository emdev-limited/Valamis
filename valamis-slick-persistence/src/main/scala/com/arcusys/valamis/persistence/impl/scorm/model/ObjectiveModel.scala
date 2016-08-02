package com.arcusys.valamis.persistence.impl.scorm.model

case class ObjectiveModel(id: Option[Long], //lfid
                                sequencingId: Long,
                                satisfiedByMeasure: Option[Boolean],
                                identifier: Option[String],
                                minNormalizedMeasure: BigDecimal,
                                isPrimary: Option[Boolean])
