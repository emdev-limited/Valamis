package com.arcusys.learn.controllers.api.social

import com.arcusys.learn.liferay.constants.StringPoolHelper
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.models.CourseResponse
import com.arcusys.learn.models.response.social._
import com.arcusys.learn.utils.LiferayGroupExtensions._
import com.arcusys.valamis.certificate.model.{Certificate, CertificateStateType, CertificateStatuses}
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.grade.model.CourseGrade
import com.arcusys.valamis.lesson.model.{CertificateActivityType, CourseActivityType, PackageActivityType, _}
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.lesson.tincan.model.{TincanManifest, TincanPackage}
import com.arcusys.valamis.social.model.UserStatus
import com.arcusys.valamis.uri.service.URIServiceContract
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.service.LayoutSetLocalServiceUtil
import com.liferay.portal.webserver.WebServerServletTokenUtil

trait ActivityInterpreter{
  def getVerb(className: String, activityType: Int): String
  def getObj(className: String, classPK: Option[Long], extraData: Option[String], plId: Option[Long] = None): Option[ActivityObjectResponse]
}

class ActivityInterpreterImpl(implicit val bindingModule: BindingModule)
  extends Injectable
  with ActivityInterpreter {

  val ScormPackageClassName = classOf[ScormPackage].getName
  val TincanPackageClassName = classOf[TincanPackage].getName
  val CertificateClassName = classOf[Certificate].getName
  val CertificateTypeClassName = CertificateStateType.getClass.getName
  val CertificateActivityTypeClassName = CertificateActivityType.getClass.getName
  val CourseGradeClassName = classOf[CourseGrade].getName
  val UserStatusClassName = classOf[UserStatus].getName
  val CourseStatus = CourseActivityType.getClass.getName

  lazy val uriService = inject[URIServiceContract]
  lazy val packageService = inject[ValamisPackageService]
  lazy val courseService = inject[CourseService]
  lazy val certificateRepository = inject[CertificateRepository]

  override def getVerb(className: String, activityType: Int): String = className match {
    case ScormPackageClassName | TincanPackageClassName => PackageActivityType(activityType).toString
    case CertificateClassName => CertificateStatuses(activityType).toString
    case CertificateTypeClassName => "Published"
    case CertificateActivityTypeClassName => CertificateActivityType(activityType).toString
    case CourseGradeClassName | CourseStatus => "Completed"
    case UserStatusClassName => "Wrote"
  }

  override def getObj(className: String, classPK: Option[Long], extraData: Option[String], plId: Option[Long] = None): Option[ActivityObjectResponse] = className match {
    case ScormPackageClassName | TincanPackageClassName => getPackageActivityObj(classPK.get, extraData, plId)
    case CertificateClassName | CertificateTypeClassName | CertificateActivityTypeClassName => Some(getCertificateActivityObj(classPK.get, extraData, plId))
    case CourseGradeClassName => Some(getCourseActivityObj(classPK.get, extraData))
    case UserStatusClassName => extraData.map(getUserStatusActivityObj)
    case CourseStatus => Some(getCourseActivityObj(classPK.get, extraData))
  }

  private def getUserStatusActivityObj(content: String) = ActivityUserStatusResponse(content)

  private def getCourseActivityObj(courseId: Long, extraData: Option[String]) = {
    val course = courseService.getById(courseId.toInt)
    val layoutSet = LayoutSetLocalServiceUtil.getLayoutSet(courseId, false)

    val logo = if (layoutSet.isLogo) {
      val token = WebServerServletTokenUtil.getToken(layoutSet.getLogoId)
      Some(s"/layout_set_logo?img_id=${layoutSet.getLogoId}&t=$token")
    }
    else
      None

    ActivityCourseResponse(courseId, course.map(_.getDescriptiveName).getOrElse(""), logo)
  }

  private def getCertificateActivityObj(certificateId: Long, extraData: Option[String], plId: Option[Long] = None) = {
    val certificate = certificateRepository.getById(certificateId.toInt)
    val logo = if (certificate.logo == "") None else Some(certificate.logo)

    ActivityCertificateResponse(certificateId, certificate.title, logo, url = Some(getCertificateURL(certificate, plId, certificate.companyId)))
  }

  private def getPackageActivityObj(packageId: Long, extraData: Option[String], plId: Option[Long] = None) = {
    val pack = packageService.getById(packageId)

    val lGroup = pack.flatMap(_.courseID).flatMap(courseService.getById(_))
    val course = lGroup.map(g =>
      CourseResponse(
        g.getGroupId,
        g.getDescriptiveName,
        g.getCourseFriendlyUrl,
        g.getDescription)
    )

    pack.map { p =>
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

  private def getPackageURL(pack: PackageBase, plId: Option[Long] = None, companyId: Long): String = {

    val className: String = pack.packageType match {
      case LessonType.Tincan => classOf[TincanManifest].getName
      case LessonType.Scorm => classOf[Manifest].getName
    }
    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(className, pack.id)

    val sb: StringBuilder = new StringBuilder(11)
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
    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(certificate.getClass.getName, certificate.id)
    val sb: StringBuilder = new StringBuilder(11)
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