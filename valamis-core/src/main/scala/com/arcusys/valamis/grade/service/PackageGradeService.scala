package com.arcusys.valamis.grade.service

import com.arcusys.valamis.course.UserCourseResultService
import com.arcusys.valamis.grade.model.PackageGrade
import com.arcusys.valamis.grade.storage.PackageGradesStorage
import com.arcusys.valamis.lesson._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

trait PackageGradeService {
  def getPackageGrade(userId: Long, packageId: Long): Option[PackageGrade]

  def getPackageGrade(userId: Long, packageIds: Seq[Long]): Seq[PackageGrade]

  def updatePackageGrade(courseId: Long, userId: Long, packageId: Long, grade: Option[Float], comment: String): Unit
}

class PackageGradeServiceImpl(implicit val bindingModule: BindingModule) extends PackageGradeService with Injectable {

  private lazy val packageGradeStorage = inject[PackageGradesStorage]
  private lazy val packageChecker = inject[PackageChecker]
  private lazy val userCourseResult = inject[UserCourseResultService]

  def getPackageGrade(userId: Long, packageId: Long): Option[PackageGrade] = {
    packageGradeStorage.get(userId, packageId)
  }

  def getPackageGrade(userId: Long, packageIds: Seq[Long]): Seq[PackageGrade] = {
    packageGradeStorage.get(userId, packageIds)
  }

  def updatePackageGrade(courseId: Long, userId: Long, packageId: Long, grade: Option[Float], comment: String) {
    getPackageGrade(userId, packageId) match {
      case Some(value) =>
        val changedPackageGrade = value.copy(comment = comment, grade = grade)
        packageGradeStorage.modify(changedPackageGrade)

      case None =>
        val packageGrade = PackageGrade(userId, packageId, grade, comment)
        packageGradeStorage.create(packageGrade)
    }

    val isCompleted = packageChecker.isCourseCompleted(courseId, userId)
    userCourseResult.set(courseId, userId, isCompleted)
  }
}
