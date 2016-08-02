package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.ActivityGoal
import com.arcusys.valamis.model.PeriodTypes.PeriodType

/**
 * Created by mminin on 04.03.15.
 */
trait ActivityGoalStorage {
  def create(certificateId: Long,
             activityName: String,
             count: Int,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             isOptional: Boolean = false,
             groupId: Option[Long] = None): ActivityGoal
  def modify(goalId: Long, count: Int): ActivityGoal
  def getBy(goalId: Long): Option[ActivityGoal]
  def get(certificateId: Long, activityName: String): Option[ActivityGoal]
  def getByCertificateId(certificateId: Long): Seq[ActivityGoal]
}