package com.arcusys.learn.models.Gradebook

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.learn.models.{CourseConverter, CourseResponse}
import com.arcusys.valamis.grade.service.PackageGradeService
import com.arcusys.valamis.lesson.PackageChecker
import com.arcusys.valamis.lesson.model.BaseManifest
import com.arcusys.valamis.lrs.tincan.Statement

case class GradedPackageResponse(
  id: Long,
  title: String,
  description: Option[String],
  course: CourseResponse,
  grade: Option[Float],
  autoGrade: Option[Float],
  sortGrade: Float
)

trait GradedPackageConverter extends CourseConverter {
  protected def gradeService: PackageGradeService
  protected def packageChecker: PackageChecker
  protected def lastCompleted(pack: BaseManifest, userId: Long): Option[Statement]

  protected def toResponse(lGroup: LGroup, userId: Long)
                          (pack: BaseManifest, autoGradePackage: Seq[(String, Option[Float])]): GradedPackageResponse = {
    val course = toResponse(lGroup)
    val grade =
      gradeService
        .getPackageGrade(userId.toInt, pack.id)
        .flatMap(_.grade)
    val autoGrade =
      packageChecker.getPackageAutoGrade(pack, userId, autoGradePackage)

    GradedPackageResponse(
      id = pack.id,
      title = pack.title,
      description = pack.summary,
      course = course,
      grade = grade,
      autoGrade = autoGrade,
      sortGrade = autoGrade.getOrElse(grade.getOrElse(0F))
    )
  }
}