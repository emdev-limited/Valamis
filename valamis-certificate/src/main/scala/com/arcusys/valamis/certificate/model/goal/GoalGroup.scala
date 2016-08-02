package com.arcusys.valamis.certificate.model.goal

import com.arcusys.valamis.model.PeriodTypes._

case class GoalGroup (id: Long,
                      count: Int,
                      certificateId: Long,
                      periodValue: Int,
                      periodType: PeriodType,
                      arrangementIndex: Int)
