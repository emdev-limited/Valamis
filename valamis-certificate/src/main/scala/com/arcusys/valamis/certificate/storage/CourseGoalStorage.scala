package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.CourseGoal
import com.arcusys.valamis.model.PeriodTypes

/**
 * Created by mminin on 04.03.15.
 */
trait CourseGoalStorage {
  def create(certificateId: Long,
             courseId: Long,
             periodValue: Int,
             periodType: PeriodTypes.Value,
             arrangementIndex: Int,
             isOptional: Boolean = false,
             groupId: Option[Long] = None): CourseGoal

  def get(certificateId: Long, courseId: Long, isDeleted: Option[Boolean] = Some(false)): Option[CourseGoal]

  def getBy(goalId: Long): Option[CourseGoal]

  def getByCertificateId(certificateId: Long, isDeleted: Option[Boolean] = Some(false)): Seq[CourseGoal]
}