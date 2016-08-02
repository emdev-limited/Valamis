package com.arcusys.valamis.persistence.impl.scorm.model

case class ActivityStateModel(id: Option[Long],
                                packageId: Option[Long],
                                activityId: String,
                                active: Boolean,
                                suspended: Boolean,
                                attemptCompleted: Option[Boolean],
                                attemptCompletionAmount: Option[BigDecimal],
                                attemptAbsoluteDuration: BigDecimal,
                                attemptExperiencedDuration: BigDecimal,
                                activityAbsoluteDuration: BigDecimal,
                                activityExperiencedDuration: BigDecimal,
                                attemptCount: Int,
                                activityStateNodeId: Option[Long],
                                activityStateTreeId: Option[Long])
