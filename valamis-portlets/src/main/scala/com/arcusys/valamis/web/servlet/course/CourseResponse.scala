package com.arcusys.valamis.web.servlet.course

import javax.servlet.http.HttpServletRequest
import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.course.util.{CourseFriendlyUrlExt, CourseInfoFriendlyUrlExt}
import com.arcusys.valamis.course.model.{CourseInfo, CourseMembershipType}
import com.arcusys.valamis.course.CourseMemberService
import com.arcusys.valamis.course.service.{CertificateService, CourseService, CourseUserQueueService}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.ratings.model.Rating
import org.joda.time.DateTime
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import com.arcusys.valamis.web.util.ForkJoinPoolWithLRCompany.ExecutionContext

/**
  * Created by Iliya Tryapitsin on 12.03.14.
  */
case class CourseResponseWithGrade(course: CourseResponse,
                                   grade: Option[Float])

case class CourseResponse(
                           id: Long,
                           title: String,
                           url: String,
                           description: String,
                           membershipType: String,
                           isActive: Boolean,
                           rating: Option[Rating] = None,
                           users: Option[Int] = None,
                           completed: Option[Int] = None,
                           hasLogo: Boolean = false,
                           isMember: Boolean = false,
                           hasRequestedMembership: Boolean = false,
                           membershipRequestsCount: Option[Int] = None,
                           tags: Seq[CourseTag] = Seq(),
                           siteRoles: Seq[SiteRole] = Seq(),
                           logoUrl: String = "",
                           friendlyUrl: String = "",
                           longDescription: Option[String] = None,
                           userLimit: Option[Int] = None,
                           beginDate: Option[DateTime] = None,
                           endDate: Option[DateTime] = None,
                           userCount: Option[Int] = None,
                           theme: Option[ThemeResponse] = None,
                           prerequisitesCompleted: Option[Boolean] = None,
                           isAvailableNow: Option[Boolean] = None,
                           isQueueMember: Option[Boolean] = None,
                           queueCount: Option[Int] = None,
                           certificates: Seq[CertificateResponse] = Seq(),
                           canEditMembers: Option[Boolean] = None,
                           canEditCourse: Option[Boolean] = None
                         )

case class SiteRole(
                     id: Long,
                     name: String,
                     description: String
                   )

case class CourseTag(id: Long, text: String)

object CourseConverter {

  def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  def toResponse(course: CourseInfo): CourseResponse = {
    CourseResponse(
      course.id,
      course.descriptiveName,
      course.getCourseFriendlyUrl,
      course.description.replace("\n", " "),
      CourseMembershipType.toValidString(course.groupType),
      course.isActive,
      friendlyUrl = course.friendlyUrl,
      longDescription = course.longDescription,
      userLimit = course.userLimit,
      beginDate = course.beginDate,
      endDate = course.endDate,
      userCount = course.userCount
    )
  }

  def toResponse(lGroup: LGroup): CourseResponse = {
    CourseResponse(
      lGroup.getGroupId,
      lGroup.getDescriptiveName,
      lGroup.getCourseFriendlyUrl,
      lGroup.getDescription.replace("\n", " "),
      CourseMembershipType.toValidString(lGroup.getType),
      lGroup.isActive,
      friendlyUrl = lGroup.getFriendlyURL
    )
  }

  def addLogoInfo(course: CourseResponse)(implicit courseService: CourseService): CourseResponse = {
    val withLogoInfo = course.copy(hasLogo = courseService.hasLogo(course.id))

    if (withLogoInfo.hasLogo) withLogoInfo.copy(logoUrl = courseService.getLogoUrl(course.id))
    else withLogoInfo
  }

  def addMembershipInfo(course: CourseResponse)(implicit r: HttpServletRequest, memberService: CourseMemberService): CourseResponse = {
    val user = Option(PortalUtilHelper.getUser(r))
    val isMemberOfCourse = user.exists(_.getGroups.asScala.exists(_.getGroupId == course.id))
    val hasRequestedMembership = !isMemberOfCourse &&
      user.map(_.getUserId).exists(memberService.getPendingMembershipRequestUserIds(course.id).contains(_))

    if (isMemberOfCourse) {
      val requestCount = memberService.getPendingMembershipRequestsCount(course.id)

      (if (requestCount > 0) course.copy(membershipRequestsCount = Some(requestCount)) else course)
        .copy(isMember = true)

    } else {
      if (hasRequestedMembership) course.copy(hasRequestedMembership = true) else course
    }
  }

  def addTags(course: CourseResponse)(implicit courseService: CourseService): CourseResponse =
    course.copy(tags = courseService.getTags(course.id).map(x => CourseTag(x.getCategoryId, x.getName)))

  def addRating(course: CourseResponse)(implicit userId: Long, courseRatingService: RatingService[LGroup]): CourseResponse = {
    course.copy(rating = Some(courseRatingService.getRating(userId, course.id)))
  }

  def addTheme(course: CourseResponse)(implicit userId: Long, courseService: CourseService): CourseResponse = {
    val theme = courseService.getTheme(course.id)
    course.copy(theme = Option(ThemeConverter.toResponse(theme)))
  }

  def addCertificatesInfo(course: CourseResponse)(implicit userId: Long,
                                                  certificateService: CertificateService): CourseResponse = {
    val certificatesResponse = certificateService.getCertificates(course.id).map(cert => {
      val userStatus = certificateService.getUserCertificateStatus(cert.id, userId).map(_.toString)
      CertificateConverter.toResponse(cert, userStatus)
    })

    course.copy(prerequisitesCompleted = Some(certificateService.prerequisitesCompleted(course.id, userId)),
      certificates = certificatesResponse)
  }

  def addDateIntervalInfo(course: CourseResponse)(implicit courseService: CourseService): CourseResponse = {
    val dateIntervalHit = courseService.isAvailableNow(course.beginDate, course.endDate)
    course.copy(isAvailableNow = Some(dateIntervalHit))
  }

  def addQueueInfo(course: CourseResponse)(implicit userId: Long,
                                           courseUserQueueService: CourseUserQueueService): CourseResponse = {
    val isQueueMemberF = courseUserQueueService.get(course.id, userId)
    val queueCountF = courseUserQueueService.count(course.id)
    await {
      for {
        queueItem <- isQueueMemberF
        queueCount <- queueCountF
      } yield course.copy(isQueueMember = Some(queueItem.isDefined), queueCount = Some(queueCount))
    }
  }
}