package com.arcusys.valamis.certificate.model.goal

case class StatementGoal(goalId: Long,
                         certificateId: Long,
                         verb: String,
                         obj: String) extends Goal