package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.StatementGoal
import com.arcusys.valamis.model.PeriodTypes

/**
 * Created by mminin on 02.03.15.
 */
trait StatementGoalStorage {

  def create(certificateId: Long,
             verb: String,
             obj: String,
             periodValue: Int,
             periodType: PeriodTypes.Value,
             arrangementIndex: Int,
             isOptional: Boolean = false,
             groupId: Option[Long] = None): StatementGoal
  def getByVerbAndObj(verb: String, obj: String): Seq[StatementGoal]
  def get(certificateId: Long, verb: String, obj: String, isDeleted: Option[Boolean] = Some(false)): Option[StatementGoal]
  def getBy(goalId: Long): Option[StatementGoal]
  def getByCertificateId(certificateId: Long, isDeleted: Option[Boolean] = Some(false)): Seq[StatementGoal]
}