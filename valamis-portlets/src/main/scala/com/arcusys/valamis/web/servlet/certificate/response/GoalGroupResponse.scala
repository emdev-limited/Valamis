package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.user.model.UserInfo
import org.joda.time.DateTime

case class GoalGroupResponse(id: Long,
                             count: Int,
                             periodValue: Int,
                             periodType: PeriodType,
                             arrangementIndex: Int,
                             modifiedDate: DateTime,
                             userId: Option[Long],
                             isDeleted: Boolean = false,
                             user: Option[UserInfo])