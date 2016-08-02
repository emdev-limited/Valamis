package com.arcusys.valamis.persistence.impl.scorm.model

import com.arcusys.valamis.lesson.scorm.model.manifest.RollupConsiderationType.RollupConsiderationType

case class RollupContributionModel(id: Option[Long],
                                   sequencingId: Long,
                                   contributeToSatisfied: Option[RollupConsiderationType],
                                   contributeToNotSatisfied: Option[RollupConsiderationType],
                                   contributeToCompleted: Option[RollupConsiderationType],
                                   contributeToIncompleted: Option[RollupConsiderationType],
                                   objectiveMeasureWeight: BigDecimal,
                                   measureSatisfactionIfActive: Boolean)
