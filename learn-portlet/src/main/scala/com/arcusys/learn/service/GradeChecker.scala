package com.arcusys.learn.service

import com.arcusys.learn.liferay.services.SocialActivityLocalServiceHelper
import com.arcusys.valamis.course.UserCourseResultService
import com.arcusys.valamis.lesson.model.{CourseActivityType, PackageActivityType}
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.lesson.tincan.model.TincanPackage
import com.arcusys.valamis.lesson.tincan.storage.TincanManifestActivityStorage
import com.arcusys.valamis.lesson.{PackageChecker, _}
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import com.arcusys.valamis.lrs.util.{TinCanVerbs, TincanHelper}
import com.arcusys.valamis.uri.model.ValamisURIType
import com.arcusys.valamis.uri.service.URIServiceContract
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

import scala.util.Try

trait GradeChecker {
  def checkCourseComplition(companyId: Long, userId: Long, statements: Seq[Statement]): Unit
}

class GradeCheckerImpl(implicit val bindingModule: BindingModule) extends GradeChecker with Injectable {

  private lazy val uriService = inject[URIServiceContract]
  private lazy val packageService = inject[ValamisPackageService]
  private lazy val tincanManifestActivityStorage = inject[TincanManifestActivityStorage]
  private lazy val packageChecker = inject[PackageChecker]
  private lazy val userCourseService = inject[UserCourseResultService]

  def checkCourseComplition(companyId: Long, userId: Long, statements: Seq[Statement]): Unit = {
    //TODO: refactor. all statements should be for same course and package, 
    // move package reading and part of isCourseComplete out of for
    for {
      statement <- statements if TincanHelper.isVerbType(statement.verb, TinCanVerbs.Completed)
      scoreRaw <- statement.result.flatMap(_.score).flatMap(_.scaled)
      // FIXME: why it can be > 1? , by standard score is 'Decimal number between -1 and 1, inclusive'
      score = if (scoreRaw <= 1) scoreRaw * 100 else scoreRaw
      if score >= LessonSuccessLimit
    } {
      val (packageIdOpt, classOfPackage) = statement.obj.asInstanceOf[Activity].id match {
        case id if id contains "course" =>
          val packageId = tincanManifestActivityStorage.getByTincanId(id).map(_.packageId)
          (packageId, classOf[TincanPackage].getName)
        case id if id contains "package" => // http://valamis/../package_234
          val uri = s"${uriService.getLocalURL()}${ValamisURIType.Package}/${ValamisURIType.Package}_"
          val packageId = Try(id.replaceFirst(uri, "").toLong).toOption
          (packageId, classOf[ScormPackage].getName)
      }

      for {
        packageId <- packageIdOpt
        courseId <- packageService.getPackage(packageId).courseId.map(_.toLong)
      } {

        SocialActivityLocalServiceHelper.addWithSet(
          companyId,
          userId,
          classOfPackage,
          courseId = Some(courseId),
          `type` = Some(PackageActivityType.Completed.id),
          classPK = Some(packageId))

        if (packageChecker.isCourseCompleted(courseId, userId)) {

          SocialActivityLocalServiceHelper.addWithSet(
            companyId,
            userId,
            CourseActivityType.getClass.getName,
            courseId = Some(courseId),
            `type` = Some(CourseActivityType.Completed.id),
            classPK = Some(courseId)
          )

          userCourseService.set(courseId, userId, isCompleted = true)
        }

      }
    }
  }
}
