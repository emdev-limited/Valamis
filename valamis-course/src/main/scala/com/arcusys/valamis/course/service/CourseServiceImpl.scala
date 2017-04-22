package com.arcusys.valamis.course.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services._
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.course.model.{CourseExtended, CourseInfo}
import com.arcusys.valamis.course.{CourseMemberService, api}
import com.arcusys.valamis.course.model.CourseMembershipType.CourseMembershipType
import com.arcusys.valamis.course.storage.{CourseCertificateRepository, CourseExtendedRepository}
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.ratings.model.Rating
import com.arcusys.valamis.tag.TagService
import org.joda.time.DateTime
import scala.collection.JavaConverters._


abstract class CourseServiceImpl extends api.CourseServiceImpl with CourseService{

  private lazy val categoryService = AssetCategoryLocalServiceHelper
  private lazy val ratingService = new RatingService[LGroup]

  def courseRepository: CourseExtendedRepository

  def courseTagService: TagService[LGroup]
  def courseMemberService: CourseMemberService

  def courseCertificateRepository: CourseCertificateRepository

  def certificateRepository: CertificateRepository

  val isMember = (gr: LGroup, user: LUser) => user.getGroups.asScala.exists(_.getGroupId == gr.getGroupId)

  private val notGuestSite = (gr: LGroup) => gr.getFriendlyURL != "/guest"

  private val hasCorrectType = (gr: LGroup) =>
    gr.getType == GroupLocalServiceHelper.TYPE_SITE_OPEN ||
      gr.getType == GroupLocalServiceHelper.TYPE_SITE_RESTRICTED ||
      gr.getType == GroupLocalServiceHelper.TYPE_SITE_PRIVATE

  private val namePatternFits = (gr: LGroup, filter: String) =>
    filter.isEmpty ||
      gr.getDescriptiveName.toLowerCase.contains(filter)

  override def getAll(companyId: Long, skipTake: Option[SkipTake], namePattern: String, sortAscDirection: Boolean): RangeResult[CourseInfo] = {
    var courses = getByCompanyId(companyId)

    if (!namePattern.isEmpty) {
      courses = courses.filter(_.getDescriptiveName.toLowerCase.contains(namePattern.toLowerCase))
    }

    val total = courses.length

    if (!sortAscDirection) courses = courses.reverse

    for (SkipTake(skip, take) <- skipTake)
      courses = courses.slice(skip, skip + take)

    val coursesInfo = attachCourseInfo(courses)

    RangeResult(total, coursesInfo)
  }

  override def getAllForUser(companyId: Long,
                             user: Option[LUser],
                             skipTake: Option[SkipTake],
                             namePattern: String,
                             sortAscDirection: Boolean,
                             isActive: Option[Boolean] = None): RangeResult[CourseInfo] = {

    val namePatternLC = namePattern.toLowerCase
    val userGroupIds = user.map(_.getUserGroupIds.toSeq).getOrElse(Seq())

    val allowedToSee = (gr: LGroup) =>
      gr.getType != GroupLocalServiceHelper.TYPE_SITE_PRIVATE ||
        userGroupIds.isEmpty ||
        userGroupIds.contains(gr.getGroupId)

    val isVisible = (gr: LGroup) => isActive.isEmpty || (gr.isActive == isActive.get)

    val allFilters = (gr: LGroup) =>
      hasCorrectType(gr) &&
        notPersonalSite(gr) &&
        allowedToSee(gr) &&
        namePatternFits(gr, namePatternLC) &&
        notGuestSite(gr) &&
        isVisible(gr)

    var courses = getByCompanyId(companyId = companyId, skipCheckActive = true).filter(allFilters)

    val total = courses.length

    if (!sortAscDirection) courses = courses.reverse

    for (SkipTake(skip, take) <- skipTake)
      courses = courses.slice(skip, skip + take)

    val coursesInfo = attachCourseInfo(courses)

    RangeResult(total, coursesInfo)
  }

  override def getNotMemberVisible(companyId: Long,
                                   user: LUser,
                                   skipTake: Option[SkipTake],
                                   namePattern: String,
                                   sortAscDirection: Boolean): RangeResult[CourseInfo] = {
    val namePatternLC = namePattern.toLowerCase

    val userOrganizations = user.getOrganizations.asScala
    val allOrganizations = OrganizationLocalServiceHelper.getOrganizations()

    val organizationsNotUser = allOrganizations.filterNot { o => userOrganizations.contains(o) }

    val organizationsGroups = organizationsNotUser.toList.map(_.getGroup).map(new LGroup(_))

    val allowedToSee = (gr: LGroup) => gr.getType != GroupLocalServiceHelper.TYPE_SITE_PRIVATE

    val allFilters = (gr: LGroup) =>
      hasCorrectType(gr) &&
        notPersonalSite(gr) &&
        !isMember(gr, user) &&
        namePatternFits(gr, namePatternLC) &&
        notGuestSite(gr) &&
        allowedToSee(gr) &&
        isVisible(gr)

    val courses = (getByCompanyId(companyId) ++ organizationsGroups).filter(allFilters)

    var coursesInfo = attachCourseInfo(courses).filter(course =>
      isAvailable(course.beginDate, course.endDate)
    )

    if (!sortAscDirection) coursesInfo = coursesInfo.reverse

    val total = coursesInfo.length

    for (SkipTake(skip, take) <- skipTake)
      coursesInfo = coursesInfo.slice(skip, skip + take)

    RangeResult(total, coursesInfo)
  }

  override def getByIdCourseInfo(courseId: Long): CourseInfo = {
    CourseInfo(groupService.getGroup(courseId))
  }

  override def getSitesByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true): RangeResult[CourseInfo] = {
    val groups = getSitesByUserId(userId)
    val result = getSortedAndOrdered(groups, skipTake, sortAsc)

    val coursesInfo = attachCourseInfo(result)

    RangeResult(groups.size, coursesInfo)
  }

  override def getByUserAndName(user: LUser,
                                skipTake: Option[SkipTake],
                                namePattern: Option[String],
                                sortAsc: Boolean): RangeResult[CourseInfo] = {

    val namePatternLC = namePattern.getOrElse("").toLowerCase

    val userOrganizations = user.getOrganizations
    val organizationGroups = userOrganizations.asScala.map(o => new LGroup(o.getGroup))


    val groups = (GroupLocalServiceHelper.getSiteGroupsByUser(user)
      .filter(isMember(_, user)) ++ organizationGroups)
      .filter(namePatternFits(_, namePatternLC))
      .filter(hasCorrectType)
      .filter(notGuestSite)
      .filter(notPersonalSite)
      .filter(isVisible)

    val result = getSortedAndOrdered(groups, skipTake, sortAsc)

    val coursesInfo = attachCourseInfo(result)

    RangeResult(groups.size, coursesInfo)
  }

  override def addCourse(companyId: Long,
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
                         templateId: Option[Long]): CourseInfo = {
    val course = GroupLocalServiceHelper.addPublicSite(
      userId,
      title,
      description,
      formatFrienlyUrl(friendlyUrl),
      membershipType.id,
      isActive,
      tags,
      companyId)

    courseRepository.create(CourseExtended(course.getGroupId, longDescription, userLimit, beginDate, endDate))

    updateTags(companyId, course, tags)

    templateId match {
      case Some(id) => createSiteBasedOnTemplate(course.getGroupId, id, themeId)
      case _ => createSiteWithOnePage(course.getGroupId, userId, themeId)
    }

    CourseInfo(course)
  }

  override def delete(courseId: Long): Unit = {
    groupService.deleteGroup(courseId)
    courseRepository.delete(courseId)
  }

  override def rateCourse(courseId: Long, userId: Long, score: Double) = {
    ratingService.updateRating(userId, score, courseId)
  }

  override def deleteCourseRating(courseId: Long, userId: Long) = {
    ratingService.deleteRating(userId, courseId)
  }

  override def getRating(courseId: Long, userId: Long): Rating = {
    ratingService.getRating(userId, courseId)
  }

  override def getLogoUrl(courseId: Long) = {
    val layoutSet = LayoutSetLocalServiceHelper.getLayoutSet(courseId, true)
    if (layoutSet.isLogo) "/image/layout_set_logo?img_id=" + layoutSet.getLogoId
    else ""
  }

  override def setLogo(courseId: Long, content: Array[Byte]) = {
    LayoutSetLocalServiceHelper.updateLogo(courseId = courseId, privateLayout = true, logo = true, content = content)
    LayoutSetLocalServiceHelper.updateLogo(courseId = courseId, privateLayout = false, logo = true, content = content)
  }

  override def deleteLogo(courseId: Long) = {
    LayoutSetLocalServiceHelper.updateLogo(courseId = courseId, privateLayout = true, logo = false, content = Array())
    LayoutSetLocalServiceHelper.updateLogo(courseId = courseId, privateLayout = false, logo = false, content = Array())
  }

  override def hasLogo(courseId: Long): Boolean = LayoutSetLocalServiceHelper.getLayoutSet(courseId, true).isLogo

  override def update(courseId: Long,
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
                      themeId: Option[String]): CourseInfo = {
    val originalGroup = groupService.getGroup(courseId)

    originalGroup.setName(title)
    originalGroup.setDescription(description.getOrElse(""))
    originalGroup.setFriendlyURL(formatFrienlyUrl(friendlyUrl))
    if (membershipType.isDefined) originalGroup.setType(membershipType.get.id)
    originalGroup.setActive(isActive.getOrElse(originalGroup.isActive))

    updateTags(companyId, originalGroup, tags)

    val course = groupService.updateGroup(originalGroup)

    val courseExtended = if (courseRepository.isExist(courseId)) {
      courseRepository.update(CourseExtended(courseId, longDescription, userLimit, beginDate, endDate))
    } else {
      courseRepository.create(CourseExtended(courseId, longDescription, userLimit, beginDate, endDate))
    }

    setTheme(courseId, themeId)

    CourseInfo(course)
  }

  override def getTags(courseId: Long): Seq[LAssetCategory] = categoryService.getCourseCategories(courseId)

  override def getTheme(courseId: Long): LTheme = {
    LayoutSetLocalServiceHelper.getLayoutSet(courseId, false).getTheme
  }

  override def setTheme(courseId: Long, themeId: Option[String]): Unit = {
    themeId match {
      case Some(id) => GroupLocalServiceHelper.setThemeToLayout(courseId, id)
      case _ => GroupLocalServiceHelper.setThemeToLayout(courseId, null)
    }
  }

  override def isAvailableNow(beginDate: Option[DateTime], endDate: Option[DateTime]): Boolean = {
    (beginDate, endDate) match {
      case (Some(beginDate), Some(endDate)) =>
        beginDate.isBeforeNow && endDate.isAfterNow
      case _ => true
    }
  }

  override def isExist(courseId: Long): Boolean = {
    GroupLocalServiceHelper.fetchGroup(courseId).nonEmpty
  }

  private def isAvailable(beginDate: Option[DateTime], endDate: Option[DateTime]): Boolean = {
    (beginDate, endDate) match {
      case (Some(beginDate), Some(endDate)) => endDate.isAfterNow
      case _ => true
    }
  }

  private def updateTags(companyId: Long, course: LGroup, tags: Seq[String]): Unit = {
    val tagIds = courseTagService.getOrCreateTagIds(tags, companyId)
    categoryService.getCourseEntryIds(course.getGroupId)
      .foreach(courseTagService.setTags(_, tagIds))
  }

  private def attachCourseInfo(courses: Seq[LGroup]) = {
    courses.map((course) => {
      val courseInfo = courseRepository.getById(course.getGroupId)
      val userCount = courseMemberService.getCountUsers(course.getGroupId)
      val courseWithInfo = courseInfo match {
        case Some(x) => CourseInfo(course).copy(
          longDescription = x.longDescription,
          userLimit = x.userLimit,
          beginDate = x.beginDate,
          endDate = x.endDate)
        case None => CourseInfo(course)
      }

      courseWithInfo.copy(userCount = Some(userCount))
    })
  }

  private def getSortedAndOrdered(courses: Seq[LGroup], skipTake: Option[SkipTake], sortAsc: Boolean = true) = {
    val ordered = if (sortAsc) {
      courses.sortBy(_.getDescriptiveName)
    }
    else {
      courses.sortBy(_.getDescriptiveName).reverse
    }

    skipTake match {
      case Some(SkipTake(skip, take)) => ordered.slice(skip, skip + take)
      case _ => ordered
    }
  }

  private def createSiteBasedOnTemplate(courseId: Long, templateId: Long, themeId: Option[String]): Unit = {
    createSiteBasedOnTemplate(courseId, templateId, themeId, false)
    createSiteBasedOnTemplate(courseId, templateId, themeId, true)
  }

  private def createSiteBasedOnTemplate(courseId: Long, templateId: Long, themeId: Option[String], privateLayout: Boolean): Unit = {
    val layoutSet = LayoutSetLocalServiceHelper.getLayoutSet(courseId, privateLayout)
    val template = LayoutSetPrototypeServiceHelper.getLayoutSetPrototype(templateId)
    layoutSet.setLayoutSetPrototypeUuid(template.getUuid)
    layoutSet.setLayoutSetPrototypeLinkEnabled(true)
    layoutSet.setPageCount(1)
    themeId.map(layoutSet.setThemeId(_))
    layoutSet.setColorSchemeId(null)
    LayoutSetLocalServiceHelper.updateLayoutSet(layoutSet)
  }

  private def createSiteWithOnePage(courseId: Long, userId: Long, themeId: Option[String]): Unit = {
    GroupLocalServiceHelper.addLayout(courseId, userId, "Home", "/home", true)
    GroupLocalServiceHelper.addLayout(courseId, userId, "Home", "/home", false)
    themeId.map(GroupLocalServiceHelper.setThemeToLayout(courseId, _))
  }

  private def formatFrienlyUrl(url: String): String = {
      if (url.startsWith("/")) url else "/" + url
  }
}