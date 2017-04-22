package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{GoalType, PackageGoal}
import com.arcusys.valamis.certificate.storage.schema.PackageGoalTableComponent
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, PackageGoalStorage}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

abstract class PackageGoalStorageImpl(val db: JdbcBackend#DatabaseDef,
                                        val driver: JdbcProfile)
  extends PackageGoalStorage
    with PackageGoalTableComponent
    with SlickProfile
    with DatabaseLayer
    with Queries {

  import driver.api._

  def certificateGoalRepository: CertificateGoalRepository

  private def getPackageAction(certificateId: Long, packageId: Long, isDeleted: Option[Boolean]) = {
    packageGoals
      .filterByCertificateId(certificateId)
      .filter(_.packageId === packageId)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }

  override def get(certificateId: Long, packageId: Long, isDeleted: Option[Boolean]) =
    execSync(getPackageAction(certificateId, packageId, isDeleted).headOption)

  override def getBy(goalId: Long): Option[PackageGoal] =
    execSync(packageGoals.filterByGoalId(goalId).result.headOption)

  override def create(certificateId: Long,
                      packageId: Long,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): PackageGoal = {

    val deletedGoal = get(certificateId, packageId, isDeleted = Some(true))

    val insertOrUpdate = deletedGoal map { goal =>
      val certificateGoal = certificateGoalRepository.getById(goal.goalId, isDeleted = Some(true))
      certificateGoalRepository.modify(
        goal.goalId,
        certificateGoal.periodValue,
        certificateGoal.periodType,
        certificateGoal.arrangementIndex,
        isOptional = false,
        groupId = None,
        oldGroupId = None,
        userId = None,
        isDeleted = false
      )
      DBIO.successful()
    } getOrElse {
      val goalId = certificateGoalRepository.create(
        certificateId,
        GoalType.Package,
        periodValue,
        periodType,
        arrangementIndex,
        isOptional,
        groupId)

      val packageGoal = PackageGoal(
        goalId,
        certificateId,
        packageId)

      packageGoals += packageGoal
    }

    val resultAction = getPackageAction(certificateId, packageId, Some(false)).head

    execSyncInTransaction(insertOrUpdate >> resultAction)
  }

  override def getByPackageId(packageId: Long): Seq[PackageGoal] =
    execSync(packageGoals.filter(_.packageId === packageId).result)

  override def getByCertificateId(certificateId: Long,
                                  isDeleted: Option[Boolean]): Seq[PackageGoal] = execSync {
    packageGoals
      .filterByCertificateId(certificateId)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }
}