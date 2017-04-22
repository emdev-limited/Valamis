package com.arcusys.valamis.certificate.model.goal

import com.arcusys.valamis.model.PeriodTypes._
import org.joda.time.DateTime

case class GoalGroup(id: Long,
                     count: Int,
                     certificateId: Long,
                     periodValue: Int,
                     periodType: PeriodType,
                     arrangementIndex: Int,
                     modifiedDate: DateTime,
                     userId: Option[Long],
                     isDeleted: Boolean = false)