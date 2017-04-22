package com.arcusys.valamis.course.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.course.model.CourseInfo
import com.arcusys.valamis.course.model.CourseMembershipType.CourseMembershipType
import com.arcusys.valamis.course.api
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.ratings.model.Rating
import org.joda.time.DateTime


trait CourseService extends api.CourseService{

  def getAll(companyId: Long,
             skipTake: Option[SkipTake],
             namePattern: String,
             sortAscDirection: Boolean): RangeResult[CourseInfo]

  def getAllForUser(companyId: Long,
                    user: Option[LUser],
                    skipTake:
                    Option[SkipTake],
                    namePattern: String,
                    sortAscDirection: Boolean,
                    isActive: Option[Boolean] = None): RangeResult[CourseInfo]

  def getNotMemberVisible(companyId: Long,
                          user: LUser,
                          skipTake:
                          Option[SkipTake],
                          namePattern: String,
                          sortAscDirection: Boolean): RangeResult[CourseInfo]

  def getByIdCourseInfo(courseId: Long): CourseInfo

  def update(courseId: Long,
             companyId: Long,
             title: String,
             description: Option[String],
             friendlyUrl: String,
             membershipType: Option[CourseMembershipType],
             isActive: Option[Boolean],
             tags: Seq[String],
             longDescription: Option[String],
             userLimit: Option[Int],
             beginDate: Option[DateTime],
             endDate: Option[DateTime],
             themeId: Option[String]): CourseInfo

  def delete(courseId: Long): Unit

  def getSitesByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true): RangeResult[CourseInfo]

  def getByUserAndName(user: LUser,
                       skipTake: Option[SkipTake],
                       namePattern: Option[String],
                       sortAsc: Boolean = true): RangeResult[CourseInfo]

  def addCourse(companyId: Long,
                userId: Long,
                title: String,
                description: Option[String],
                friendlyUrl: String,
                membershipType: CourseMembershipType,
                isActive: Boolean,
                tags: Seq[String],
                longDescription: Option[String],
                userLimit: Option[Int],
                beginDate: Option[DateTime],
                endDate: Option[DateTime],
                themeId: Option[String],
                templateId: Option[Long]): CourseInfo

  def rateCourse(courseId: Long, userId: Long, score: Double): Rating

  def deleteCourseRating(courseId: Long, userId: Long): Rating

  def getRating(courseId: Long, userId: Long): Rating

  def getLogoUrl(courseId: Long): String

  def setLogo(courseId: Long, content: Array[Byte])

  def deleteLogo(courseId: Long)

  def hasLogo(courseId: Long): Boolean

  def getTags(courseId: Long): Seq[LAssetCategory]

  def getTheme(courseId: Long): LTheme

  def isAvailableNow(beginDate: Option[DateTime], endDate: Option[DateTime]): Boolean

  def isExist(courseId: Long): Boolean

  def setTheme(courseId: Long, themeId: Option[String]): Unit
}