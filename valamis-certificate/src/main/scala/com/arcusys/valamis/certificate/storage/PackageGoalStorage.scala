package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.PackageGoal
import com.arcusys.valamis.model.PeriodTypes

/**
 * Created by mminin on 02.03.15.
 */
trait PackageGoalStorage {

  def create(certificateId: Long,
             packageId: Long,
             periodValue: Int,
             periodType: PeriodTypes.Value,
             arrangementIndex: Int,
             isOptional: Boolean = false,
             groupId: Option[Long] = None): PackageGoal
  def getByPackageId(packageId: Long): Seq[PackageGoal]
  def get(certificateId: Long, packageId: Long, isDeleted: Option[Boolean] = Some(false)): Option[PackageGoal]
  def getBy(goalId: Long): Option[PackageGoal]
  def getByCertificateId(certificateId: Long, isDeleted: Option[Boolean] = Some(false)): Seq[PackageGoal]
}