package com.arcusys.valamis.certificate.repository

import javax.inject.Inject

import com.arcusys.valamis.certificate.model.goal.PackageGoal
import com.arcusys.valamis.certificate.schema.PackageGoalTableComponent
import com.arcusys.valamis.certificate.storage.PackageGoalStorage
import com.arcusys.valamis.core.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.model.PeriodTypes._
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class PackageGoalStorageImpl @Inject() (val db: JdbcBackend#DatabaseDef,
                                        val driver: JdbcProfile)
  extends PackageGoalStorage
  with PackageGoalTableComponent
  with SlickProfile {

  import driver.simple._

  def create(packageGoal: PackageGoal) = db.withSession { implicit session =>
    packageGoals.insert(packageGoal)
    packageGoals.filter(ag => ag.certificateId === packageGoal.certificateId && ag.packageId === packageGoal.packageId).first
  }

  override def get(certificateId: Long, packageId: Long) = db.withSession { implicit session =>
    packageGoals.filter(ag => ag.certificateId === certificateId && ag.packageId === packageId).firstOption
  }

  override def delete(certificateId: Long, packageId: Long) = db.withSession { implicit session =>
    packageGoals.filter(ag => ag.certificateId === certificateId && ag.packageId === packageId).delete
  }

  override def create(certificateId: Long, packageId: Long, periodValue: Int, periodType: PeriodType): PackageGoal = {
    val packageGoal = PackageGoal(certificateId, packageId, periodValue, periodType)

    db.withSession { implicit session =>
      packageGoals.insert(packageGoal)
      packageGoals.filter(ag => ag.certificateId === packageGoal.certificateId && ag.packageId === packageGoal.packageId).first
    }
  }

  override def modify(certificateId: Long, packageId: Long, periodValue: Int, periodType: PeriodType): PackageGoal = {
    val packageGoal = PackageGoal(certificateId, packageId, periodValue, periodType)

    db.withSession { implicit session =>
      val filtered =
        packageGoals
          .filter(entity => entity.certificateId === certificateId && entity.packageId === packageId)

      filtered.update(packageGoal)
      filtered.first
    }
  }

  override def getByPackageId(packageId: Long): Seq[PackageGoal] = db.withSession { implicit session =>
    packageGoals.filter(_.packageId === packageId).run
  }

  override def getByCertificateId(certificateId: Long): Seq[PackageGoal] = db.withSession { implicit session =>
    packageGoals.filter(_.certificateId === certificateId).run
  }

  override def getByCertificateIdCount(certificateId: Long): Int = db.withSession { implicit session =>
    packageGoals.filter(_.certificateId === certificateId).length.run
  }
}
