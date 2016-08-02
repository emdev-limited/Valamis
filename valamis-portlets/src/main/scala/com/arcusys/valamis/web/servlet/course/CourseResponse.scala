package com.arcusys.valamis.web.servlet.course

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.learn.liferay.services.UserGroupRoleLocalServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.course.{CourseMemberService, CourseService}
import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.course.{CourseMemberService, CourseService}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.ratings.model.Rating

import scala.collection.JavaConverters._

/**
 * Created by Iliya Tryapitsin on 12.03.14.
 */

case class CourseResponse(
  id: Long,
  title: String,
  url: String,
  description: String,
  membershipType: String,
  isActive: Boolean,
  rating: Option[Rating] = None,
  //tags: Seq[String],
  users: Option[Int] = None,
  completed: Option[Int] = None,
  hasLogo: Boolean = false,
  isMember: Boolean = false,
  hasRequestedMembership: Boolean = false,
  membershipRequestsCount: Option[Int] = None,
  timeStamp: Option[String] = None,
  tags: Seq[CourseTag] = Seq(),
  siteRoles: Seq[SiteRole] = Seq(),
  logoUrl: String = "",
  friendlyUrl: String = ""
  )

case class SiteRole(
   id:Long,
   name:String,
   description:String
   )

case class CourseTag(id:Long,text:String)

object CourseConverter {
  def toResponse(lGroup: LGroup): CourseResponse =
    CourseResponse(
      lGroup.getGroupId,
      lGroup.getDescriptiveName,
      lGroup.getCourseFriendlyUrl,
      lGroup.getDescription.replace("\n", " "),
      CourseMembershipType.toValidString(lGroup.getType),
	    lGroup.isActive,
      friendlyUrl = lGroup.getFriendlyURL
    )

  def addLogoInfo(course: CourseResponse)(implicit courseService: CourseService): CourseResponse = {
    val withLogoInfo = course.copy(hasLogo = courseService.hasLogo(course.id))

    if(withLogoInfo.hasLogo) withLogoInfo.copy(logoUrl = courseService.getLogoUrl(course.id))
    else withLogoInfo
  }

  def addMembershipInfo(course: CourseResponse)(implicit r: HttpServletRequest, memberService: CourseMemberService): CourseResponse = {
    val user =  Option(PortalUtilHelper.getUser(r))
    val isMemberOfCourse = user.exists(_.getGroups.asScala.exists(_.getGroupId == course.id))
    val hasRequestedMembership = !isMemberOfCourse &&
      user.map(_.getUserId).exists(memberService.getPendingMembershipRequestUserIds(course.id).contains(_))

    if(isMemberOfCourse) {
      val requestCount = memberService.getPendingMembershipRequestsCount(course.id)

      (if(requestCount > 0) course.copy(membershipRequestsCount = Some(requestCount)) else course)
      .copy(isMember = true)

    } else {
      if(hasRequestedMembership) course.copy(hasRequestedMembership = true) else course
    }
  }

  def addTimeStamp(course: CourseResponse):CourseResponse =
    course.copy(timeStamp = Some(System.currentTimeMillis.toString))

  def addTags(course: CourseResponse)(implicit courseService: CourseService): CourseResponse =
    course.copy(tags = courseService.getTags(course.id).map(x => CourseTag(x.getCategoryId, x.getName)))

  def addRating(course: CourseResponse)(implicit userId: Long, courseRatingService: RatingService[LGroup]):CourseResponse = {
    course.copy(rating = Some(courseRatingService.getRating(userId, course.id)))
  }
}