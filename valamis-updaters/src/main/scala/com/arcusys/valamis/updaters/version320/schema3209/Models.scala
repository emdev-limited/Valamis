package com.arcusys.valamis.updaters.version320.schema3209

import org.joda.time.DateTime

case class TrainingEventGoal(goalId: Long,
                             certificateId: Long,
                             eventId: Long)

case class Certificate(id: Long,
                       title: String,
                       description: String,
                       logo: String = "",
                       isPermanent: Boolean = true,
                       isPublishBadge: Boolean = false,
                       @deprecated
                       shortDescription: String = "",
                       companyId: Long,
                       validPeriodType: String,
                       validPeriod: Int = 0,
                       createdAt: DateTime,
                       activationDate: Option[DateTime] = None,
                       isActive: Boolean = false,
                       @deprecated
                       scope: Option[Long] = None)

case class CertificateGoal(id: Long,
                           certificateId: Long,
                           goalType: Int,
                           periodValue: Int,
                           periodType: String,
                           arrangementIndex: Int,
                           isOptional: Boolean = false,
                           groupId: Option[Long],
                           oldGroupId: Option[Long],
                           modifiedDate: DateTime,
                           userId: Option[Long],
                           isDeleted: Boolean = false)
