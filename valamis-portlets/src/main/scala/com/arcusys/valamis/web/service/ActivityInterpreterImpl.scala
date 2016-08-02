package com.arcusys.valamis.web.service

import com.arcusys.learn.liferay.LiferayClasses.LSocialActivityFeedEntry
import com.arcusys.learn.liferay.constants.StringPoolHelper
import com.arcusys.learn.liferay.services.{AssetEntryLocalServiceHelper, LayoutSetLocalServiceHelper, WebServerServletTokenHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.certificate.model.{Certificate, CertificateActivityType, CertificateStateType, CertificateStatuses}
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.gradebook.model.{CourseActivityType, CourseGrade}
import com.arcusys.valamis.lesson.model.{Lesson, PackageActivityType}
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.social.model.UserStatus
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.web.servlet.course.CourseResponse
import com.arcusys.valamis.web.servlet.social.response._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

trait ActivityInterpreter{
  def getVerb(className: String, activityType: Int): String
  def getObj(className: String,
             classPK: Option[Long],
             extraData: Option[String],
             plId: Option[Long] = None,
             lFeedEntry: Option[LSocialActivityFeedEntry],
             isSecure: Boolean = false): Option[ActivityObjectResponse]
}

class ActivityInterpreterImpl(implicit val bindingModule: BindingModule)
  extends Injectable
  with ActivityInterpreter {

  val LessonClassName = classOf[Lesson].getName
  val CertificateClassName = classOf[Certificate].getName
  val CertificateTypeClassName = CertificateStateType.getClass.getName
  val CertificateActivityTypeClassName = CertificateActivityType.getClass.getName
  val CourseGradeClassName = classOf[CourseGrade].getName
  val UserStatusClassName = classOf[UserStatus].getName
  val CourseStatus = CourseActivityType.getClass.getName

  lazy val uriService = inject[TincanURIService]
  lazy val courseService = inject[CourseService]
  lazy val certificateRepository = inject[CertificateRepository]
  lazy val lessonService = inject[LessonService]

  override def getVerb(className: String, activityType: Int): String = className match {
    case LessonClassName => PackageActivityType(activityType).toString
    case CertificateClassName => CertificateStatuses(activityType).toString
    case CertificateTypeClassName => "Published"
    case CertificateActivityTypeClassName => CertificateActivityType(activityType).toString
    case CourseGradeClassName | CourseStatus => "Completed"
    case UserStatusClassName => "Wrote"
    case other if other.contains("com.liferay") => ""
  }

  private def getLiferayActivityObj(id: Long, className: String,lFeedEntry: Option[LSocialActivityFeedEntry]): ActivityObjectResponse = {
      LActivityEntryResponse(
        id = id,
        title = lFeedEntry.fold("")(_.getTitle),
        body = lFeedEntry.fold("")(_.getBody)
      )
  }

  override def getObj(className: String,
                      classPK: Option[Long],
                      extraData: Option[String],
                      plId: Option[Long] = None,
                      lFeedEntry: Option[LSocialActivityFeedEntry],
                      isSecure: Boolean = false): Option[ActivityObjectResponse] = {
    className match {
      case LessonClassName => getPackageActivityObj(classPK.get, extraData, plId, isSecure)
      case CertificateClassName | CertificateTypeClassName | CertificateActivityTypeClassName =>
        getCertificateActivityObj(classPK.get, extraData, plId, isSecure)
      case CourseGradeClassName => Some(getCourseActivityObj(classPK.get, extraData))
      case UserStatusClassName => extraData.map(getUserStatusActivityObj)
      case CourseStatus => Some(getCourseActivityObj(classPK.get, extraData))
      case other if other.contains("com.liferay") => Some(getLiferayActivityObj(classPK.get, className, lFeedEntry))
    }
  }

  private def getUserStatusActivityObj(content: String) = ActivityUserStatusResponse(content)

  private def getCourseActivityObj(courseId: Long, extraData: Option[String]) = {
    val course = courseService.getById(courseId.toInt)
    val layoutSet = LayoutSetLocalServiceHelper.getLayoutSet(courseId, false)

    val logo = if (layoutSet.isLogo) {
      val token = WebServerServletTokenHelper.getToken(layoutSet.getLogoId)
      Some(s"/layout_set_logo?img_id=${layoutSet.getLogoId}&t=$token")
    }
    else
      None

    ActivityCourseResponse(courseId, course.map(_.getDescriptiveName).getOrElse(""), logo)
  }

  private def getCertificateActivityObj(certificateId: Long,
                                        extraData: Option[String],
                                        plId: Option[Long] = None,
                                        isSecure: Boolean = false): Option[ActivityCertificateResponse] = {
    certificateRepository.getByIdOpt(certificateId.toInt).map { certificate =>
      val logo = if (certificate.logo == "") None else Some(certificate.logo)
      ActivityCertificateResponse(certificateId, certificate.title, logo, url = Some(getCertificateURL(certificate, plId, certificate.companyId)))
    }
  }

  private def getPackageActivityObj(packageId: Long, extraData: Option[String], plId: Option[Long] = None, isSecure: Boolean = false) = {
    val lesson = lessonService.getLesson(packageId)

    val lGroup = lesson.map(_.courseId).flatMap(courseService.getById)
    val course = lGroup.map(g =>
      CourseResponse(
        g.getGroupId,
        g.getDescriptiveName,
        g.getCourseFriendlyUrl,
        g.getDescription,
        CourseMembershipType.apply(g.getType).toString,
        g.isActive)
    )

    lesson.map { p =>
      ActivityPackageResponse(
        packageId,
        p.title,
        p.logo,
        course,
        extraData,
        url = lGroup.map(g => getPackageURL(p, plId, g.getCompanyId))
      )
    }
  }

  private def getPackageURL(lesson: Lesson, plId: Option[Long] = None, companyId: Long): String = {

    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(LessonClassName, lesson.id)

    val sb = new StringBuilder()
    sb.append(PortalUtilHelper.getLocalHostUrl(companyId, false))
    sb.append(PortalUtilHelper.getPathMain)
    sb.append("/portal/learn-portlet/open_package")
    sb.append(StringPoolHelper.QUESTION)
    sb.append("plid")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(plId.getOrElse("")))
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("resourcePrimKey")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(assetEntry.getEntryId))

    sb.toString
  }

  private def getCertificateURL(certificate: Certificate, plId: Option[Long] = None, companyId: Long): String = {
    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry( certificate.getClass.getName, certificate.id)
    val sb: StringBuilder = new StringBuilder()
    sb.append(PortalUtilHelper.getLocalHostUrl(companyId, false))
    sb.append(PortalUtilHelper.getPathMain)
    sb.append("/portal/learn-portlet/open_certificate")
    sb.append(StringPoolHelper.QUESTION)
    sb.append("plid")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(plId.getOrElse("")))
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("resourcePrimKey")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(assetEntry.getEntryId))

    sb.toString
  }
}
