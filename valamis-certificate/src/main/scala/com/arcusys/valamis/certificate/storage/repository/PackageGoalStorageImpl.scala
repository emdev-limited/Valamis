package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{GoalType, PackageGoal}
import com.arcusys.valamis.certificate.storage.schema.PackageGoalTableComponent
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, PackageGoalStorage}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class PackageGoalStorageImpl(val db: JdbcBackend#DatabaseDef,
                                        val driver: JdbcProfile)
  extends PackageGoalStorage
    with PackageGoalTableComponent
  with SlickProfile {

  import driver.simple._

  def certificateGoalRepository: CertificateGoalRepository

  override def get(certificateId: Long, packageId: Long) = db.withSession { implicit session =>
    packageGoals.filter(ag => ag.certificateId === certificateId && ag.packageId === packageId).firstOption
  }

  override def getBy(goalId: Long): Option[PackageGoal] = db.withTransaction { implicit session =>
    packageGoals.filter(_.goalId === goalId).firstOption
  }

  override def create(certificateId: Long,
                      packageId: Long,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): PackageGoal = {
    db.withTransaction { implicit session =>
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

      packageGoals insert packageGoal
      packageGoals.filter(ag => ag.certificateId === certificateId && ag.packageId === packageId).first
    }
  }

  override def getByPackageId(packageId: Long): Seq[PackageGoal] = db.withSession { implicit session =>
    packageGoals.filter(_.packageId === packageId).run
  }

  override def getByCertificateId(certificateId: Long): Seq[PackageGoal] = db.withSession { implicit session =>
    packageGoals.filter(_.certificateId === certificateId).run
  }
}