package com.arcusys.valamis.certificate.model.goal

case class ActivityGoal(goalId: Long,
                        certificateId: Long,
                        activityName: String,
                        count: Int) extends Goal