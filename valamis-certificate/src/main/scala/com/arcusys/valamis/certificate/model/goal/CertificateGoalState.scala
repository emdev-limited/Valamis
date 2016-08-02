package com.arcusys.valamis.certificate.model.goal

import org.joda.time.DateTime

case class CertificateGoalState(userId: Long,
                                certificateId: Long,
                                goalId: Long,
                                status: GoalStatuses.Value,
                                modifiedDate: DateTime,
                                isOptional: Boolean = false)



