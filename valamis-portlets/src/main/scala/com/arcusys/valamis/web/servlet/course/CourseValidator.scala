package com.arcusys.valamis.web.servlet.course

import javax.servlet.http.HttpServletRequest
import com.arcusys.learn.liferay.services.{LayoutSetPrototypeServiceHelper, ThemeLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.course.exception._
import com.arcusys.valamis.course.model.CourseInfo
import com.arcusys.valamis.course.service.{CertificateService, CourseService, CourseUserQueueService, InstructorService}
import com.arcusys.valamis.web.portlet.base.ModifyPermission
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.base.exceptions.AccessDeniedException
import org.joda.time.DateTime
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by amikhailov on 24.11.16.
  */
class CourseValidator(implicit val certificateService: CertificateService,
                      val courseService: CourseService,
                      val instructorService: InstructorService,
                      val courseUserQueueService: CourseUserQueueService) {

  def validateDateInterval(beginDate: Option[DateTime], endDate: Option[DateTime]): Unit = {
    (beginDate, endDate) match {
      case (Some(begin), Some(end)) =>
        if (begin isAfter end) throw new DateNotValidException("Begin date must be before end date")
      case (Some(_), None) => throw new DateNotValidException("End date field is required.")
      case (None, Some(_)) => throw new DateNotValidException("Begin date field is required.")
      case _ =>
    }
  }

  def validateThemeId(companyId: Long, themeId: Option[String]): Unit = {
    themeId match {
      case (Some(themeId)) =>
        if (ThemeLocalServiceHelper.fetchTheme(companyId, themeId).isEmpty) {
          throw new ThemeNotFoundException(s"Theme not found, id $themeId")
        }
      case None =>
    }
  }

  def validateTemplateId(templateId: Option[Long]): Unit = {
    templateId match {
      case (Some(templateId)) =>
        if (LayoutSetPrototypeServiceHelper.fetchLayoutSetPrototype(templateId).isEmpty) {
          throw new TemplateNotFoundException(s"Template not found, id $templateId")
        }
      case None =>
    }
  }

  def validateCertificateIds(companyId: Long, certificateIds: Seq[Long]): Unit = {
    certificateIds.foreach(certificateId => {
      if (!certificateService.isExist(companyId, certificateId)) {
        throw new CertificateNotFoundException(s"Certificate not found, id $certificateId")
      }
    })
  }

  def validateJoinCourse(course: CourseInfo, userId: Long): Unit = {
    if (!courseService.isAvailableNow(course.beginDate, course.endDate)) {
      throw new DateNotValidException("Course is not available now")
    }

    if (!certificateService.prerequisitesCompleted(course.id, userId)) {
      throw new OutstandingPrerequisitesException("Certificates required")
    }
  }

  def validateCourseCapacity(course: CourseInfo, countToAdd: Int): Unit = {
    course.userLimit match {
      case Some(capacity) =>
        val free = capacity - course.userCount.getOrElse(0)
        if (free - countToAdd < 0) throw new UserLimitException("Not enough capacity for the course")
      case None => // Don't do anything
    }
  }

  def validateUserIds(userIds: Seq[Long]): Unit = {
    userIds.map(userId => if (UserLocalServiceHelper().fetchUser(userId).isEmpty) {
      throw new UserNotFoundException(s"User with id {$userId} not found")
    })
  }

  def validateUserLimit(courseId: Long, userLimit: Option[Int]): Unit = {
    userLimit match {
      case None =>
        val queueCount = await {
          courseUserQueueService.count(courseId)
        }
        if (0 < queueCount) throw new QueueNotEmptyException()
      case Some(limit) => if (limit <= 0) throw new UserLimitException("User limit must be a positive number")
    }
  }

  def validateEditMembersPermission(courseId: Long, userId: Long)(implicit request: HttpServletRequest): Unit = {
    if (!canEditMembers(courseId, userId)) throw AccessDeniedException()
  }

  def canEditMembers(courseId: Long, userId: Long)(implicit request: HttpServletRequest): Boolean =
    PermissionUtil.hasPermissionApi(ModifyPermission, PortletName.AllCourses) ||
      instructorService.isExist(courseId, userId)

  def canEditCourse()(implicit request: HttpServletRequest): Boolean =
    PermissionUtil.hasPermissionApi(ModifyPermission, PortletName.AllCourses)

  private def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)
}