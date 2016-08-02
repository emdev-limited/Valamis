package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.model.PeriodTypes._

case class GoalGroupResponse(id: Long,
                             count: Int,
                             periodValue: Int,
                             periodType: PeriodType,
                             arrangementIndex: Int)
