package com.arcusys.valamis.lesson

import com.arcusys.valamis.lesson.model.{BaseManifest, PackageBase}

trait PackageChecker {
  def getCompletedPackagesCount(courseId: Long, userId: Long): Int

  def getPackageAutoGrade(pack: PackageBase, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Option[Float]

  def getPackageAutoGrade(pack: BaseManifest, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Option[Float]

  def isPackageComplete(pack: PackageBase, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Boolean

  def isPackageComplete(pack: BaseManifest, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Boolean

  def isCourseCompleted(courseId: Long, userId: Long, packagesCount: Option[Long] = None): Boolean
}
