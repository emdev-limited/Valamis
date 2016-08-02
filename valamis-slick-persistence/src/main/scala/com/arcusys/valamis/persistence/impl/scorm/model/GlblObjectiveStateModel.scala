package com.arcusys.valamis.persistence.impl.scorm.model

case class GlblObjectiveStateModel(id: Option[Long],
                                   satisfied: Option[Boolean],
                                   normalizedMeasure: Option[BigDecimal],
                                   attemptCompleted: Option[Boolean],
                                   mapKey: String,
                                   treeId: Long)

