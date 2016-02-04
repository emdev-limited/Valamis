package com.arcusys.valamis.grade.storage

import com.arcusys.valamis.grade.model.PackageGrade

trait PackageGradesStorage {
  def get(userId: Long, packageId: Long): Option[PackageGrade]

  def get(userId: Long, packageIds: Seq[Long]): Seq[PackageGrade]

  def delete(userId: Long, packageId: Long): Unit

  def modify(entity: PackageGrade): PackageGrade

  def create(entity: PackageGrade): PackageGrade
}
