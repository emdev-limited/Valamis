package com.arcusys.valamis.lesson.service

import java.net.URI

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.grade.service.PackageGradeService
import com.arcusys.valamis.lesson.model.{BaseManifest, LessonType, PackageBase}
import com.arcusys.valamis.lesson.tincan.storage.TincanManifestActivityStorage
import com.arcusys.valamis.lesson.{PackageChecker, _}
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.util.{TinCanVerbs, TincanHelper}
import com.arcusys.valamis.lrs.util.TincanHelper._
import com.arcusys.valamis.uri.model.ValamisURIType
import com.arcusys.valamis.uri.storage.TincanURIStorage
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class PackageCheckerImpl(implicit val bindingModule: BindingModule) extends PackageChecker with Injectable {

  private lazy val lrsClient = inject[LrsClientManager]
  private lazy val packageService = inject[ValamisPackageService]

  private lazy val tincanManifestStorage = inject[TincanManifestActivityStorage]
  private lazy val tincanURIStorage = inject[TincanURIStorage]
  private lazy val packageGradeService = inject[PackageGradeService]
  private lazy val userService = inject[UserLocalServiceHelper]

  def getCompletedPackagesCount(courseId: Long, userId: Long): Int = {
    val agent = JsonHelper.toJson(userService.getUser(userId).getAgentByUuid, new AgentSerializer)
    val autoGradePackage = lrsClient.scaleApi(_.getMaxActivityScale(agent, new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Completed)))).get
    val autoGradePackagePassed = lrsClient.scaleApi(_.getMaxActivityScale(agent, new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Passed)))).get
    val packageComplete = packageService.getPackagesByCourse(courseId)
      .count { pack => isPackageComplete(pack, userId, autoGradePackage) }
    val packagePassed = packageService.getPackagesByCourse(courseId)
      .count { pack => isPackageComplete(pack, userId, autoGradePackagePassed) }

    packageComplete+packagePassed
  }

  def getPackageAutoGrade(pack: BaseManifest, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Option[Float] =
    getPackageAutoGrade(pack.getType, pack.id, userId, autoGradePackage)

  def getPackageAutoGrade(pack: PackageBase, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Option[Float] =
    getPackageAutoGrade(pack.packageType, pack.id, userId, autoGradePackage)

  private def getPackageAutoGrade(lType: LessonType.Value, packageId: Long, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Option[Float] = {
    val uri = lType match {
      case LessonType.Scorm =>
        tincanURIStorage.getById(packageId.toString, ValamisURIType.Package).map(_.uri)
      case LessonType.Tincan =>
        tincanManifestStorage.getByPackageId(packageId).headOption.map(_.tincanId)
    }

    val autoGrade = autoGradePackage.collectFirst {
      case g if uri.contains(g._1) => g._2
    } flatten

    autoGrade match {
      case Some(g) if g <= 1 => Some(g * 100)
      case Some(g) if g > 1 => Some(g)
      case None => None
    }
  }

  def isPackageComplete(pack: BaseManifest, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Boolean =
    isPackageComplete(pack.getType, pack.id, userId, autoGradePackage)

  def isPackageComplete(pack: PackageBase, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Boolean =
    isPackageComplete(pack.packageType, pack.id, userId, autoGradePackage)

  private def isPackageComplete(lType: LessonType.Value, packageId: Long, userId: Long, autoGradePackage: Seq[(String, Option[Float])]): Boolean = {

    val autoGrade = getPackageAutoGrade(lType, packageId, userId, autoGradePackage)

    if (isGradeMoreSuccessLimit(autoGrade)) true
    else {
      val teacherGrade = packageGradeService.getPackageGrade(userId, packageId).flatMap(_.grade)
      isGradeMoreSuccessLimit(teacherGrade)
    }
  }

  private def isGradeMoreSuccessLimit(grade: Option[Float]): Boolean= {
    grade match {
      case Some(g) if (g <= 1 && (g * 100) > LessonSuccessLimit) => true
      case Some(g) if (g > 1 && g > LessonSuccessLimit) => true
      case _ => false
    }
  }

  def isCourseCompleted(courseId: Long, userId: Long, packagesCount: Option[Long]) = {
    val targetCount = packagesCount getOrElse packageService.getPackagesCount(courseId.toInt)

    targetCount == getCompletedPackagesCount(courseId, userId)
  }
}
