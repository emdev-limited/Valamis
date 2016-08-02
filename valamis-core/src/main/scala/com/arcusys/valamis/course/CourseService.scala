package com.arcusys.valamis.course

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.arcusys.learn.liferay.services._
import com.arcusys.valamis.course.model.CourseMembershipType.CourseMembershipType
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.ratings.model.Rating
import com.arcusys.valamis.tag.TagService
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

import scala.collection.JavaConverters._

trait CourseService {
  def getAll: Seq[LGroup]

  def getAll(companyId: Long,
             skipTake: Option[SkipTake],
             namePattern: String,
             sortAscDirection: Boolean): RangeResult[LGroup]

  def getAllForUser(companyId: Long,
                    user: Option[LUser],
                    skipTake:
                    Option[SkipTake],
                    namePattern: String,
                    sortAscDirection: Boolean,
                    isActive: Option[Boolean] = None): RangeResult[LGroup]

  def getNotMemberVisible(companyId: Long,
                          user: LUser,
                          skipTake:
                          Option[SkipTake],
                          namePattern: String,
                          sortAscDirection: Boolean): RangeResult[LGroup]
  
  def getByCompanyId(companyId: Long, skipCheckActive: Boolean = false): Seq[LGroup]

  def getById(courseId: Long): Option[LGroup]

  def update(courseId: Long,
             companyId: Long,
             title: String,
             description:Option[String],
             friendlyUrl:Option[String],
             membershipType: Option[CourseMembershipType],
             isActive: Option[Boolean],
             tags: Seq[String]): LGroup

  def delete(courseId:Long) : Unit

  def getGroupIdsForAllCoursesFromAllCompanies: Seq[Long]

  def getByUserId(userId: Long): Seq[LGroup]

  def getSitesByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true): RangeResult[LGroup]

  def getSitesByUserId(userId: Long): Seq[LGroup]

  def getByUserId(userId: Long,
                  skipTake: Option[SkipTake],
                  sortAsc: Boolean = true): RangeResult[LGroup]

  def getByUserAndName(user: LUser,
                       skipTake: Option[SkipTake],
                       namePattern: Option[String],
                       sortAsc: Boolean = true): RangeResult[LGroup]

  def getCompanyIds: Seq[Long]

  def addCourse(companyId: Long,
                userId: Long,
                title: String,
                description:Option[String],
                friendlyUrl:Option[String],
                membershipType: CourseMembershipType,
                isActive: Boolean,
                tags: Seq[String]): LGroup

  def rateCourse(courseId:Long, userId: Long, score:Double):Rating

  def deleteCourseRating(courseId: Long, userId: Long): Rating

  def getRating(courseId: Long,userId: Long): Rating

  def getLogoUrl(courseId: Long): String

  def setLogo(courseId: Long, content: Array[Byte])

  def hasLogo(courseId: Long): Boolean

  def getTags(courseId: Long): Seq[LAssetCategory]
}

class CourseServiceImpl(implicit val bindingModule: BindingModule)
  extends CourseService
  with Injectable  {

  private lazy val groupService = GroupLocalServiceHelper
  private lazy val categoryService = AssetCategoryLocalServiceHelper
  private lazy val ratingService = new RatingService[LGroup]
  private lazy val courseTagService = inject[TagService[LGroup]]

  private val isVisible = (gr: LGroup) => gr.isActive
  val isMember = (gr: LGroup, user: LUser) => user.getGroups.asScala.exists(_.getGroupId == gr.getGroupId)
  private val notGuestSite = (gr: LGroup) => gr.getFriendlyURL != "/guest"
  private val hasCorrectType = (gr: LGroup) =>
    gr.getType == GroupLocalServiceHelper.TYPE_SITE_OPEN ||
    gr.getType == GroupLocalServiceHelper.TYPE_SITE_RESTRICTED ||
    gr.getType == GroupLocalServiceHelper.TYPE_SITE_PRIVATE

  private val notPersonalSite = (gr: LGroup) => !(gr.isUserPersonalSite || gr.isUser)

  private val namePatternFits = (gr: LGroup, filter: String) =>
    filter.isEmpty ||
    gr.getDescriptiveName.toLowerCase.contains(filter)

  def getAll: Seq[LGroup] = {
    groupService.getGroups.asScala
  }

  def getAll(companyId: Long, skipTake: Option[SkipTake], namePattern: String, sortAscDirection: Boolean): RangeResult[LGroup] = {
    var courses = getByCompanyId(companyId)

   if (!namePattern.isEmpty)
     courses = courses.filter(_.getDescriptiveName.toLowerCase.contains(namePattern.toLowerCase))

    val total = courses.length

    if (!sortAscDirection) courses = courses.reverse

    for(SkipTake(skip, take) <- skipTake)
      courses = courses.slice(skip, skip + take)

    RangeResult(total, courses)
  }

  override def getAllForUser(companyId: Long,
                             user: Option[LUser],
                             skipTake: Option[SkipTake],
                             namePattern: String,
                             sortAscDirection: Boolean,
                             isActive: Option[Boolean] = None): RangeResult[LGroup] = {

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

    for(SkipTake(skip, take) <- skipTake)
      courses = courses.slice(skip, skip + take)

    RangeResult(total, courses)
  }


  override def getNotMemberVisible(companyId: Long,
                                   user: LUser,
                                   skipTake: Option[SkipTake],
                                   namePattern: String,
                                   sortAscDirection: Boolean): RangeResult[LGroup] = {
    val namePatternLC = namePattern.toLowerCase

    val userOrganizations = user.getOrganizations.asScala
    val allOrganizations = OrganizationLocalServiceHelper.getOrganizations(QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS).asScala

    val organizationsNotUser = allOrganizations.filterNot{o => userOrganizations.contains(o)}

    val organizationsGroups = organizationsNotUser.toList.map(_.getGroup)

    val allowedToSee = (gr: LGroup) => gr.getType != GroupLocalServiceHelper.TYPE_SITE_PRIVATE

    val allFilters = (gr: LGroup) =>
      hasCorrectType(gr) &&
      notPersonalSite(gr) &&
      !isMember(gr, user) &&
      namePatternFits(gr, namePatternLC) &&
      notGuestSite(gr) &&
      allowedToSee(gr) &&
      isVisible(gr)

    var courses = (getByCompanyId(companyId) ++ organizationsGroups).filter(allFilters)

    val total = courses.length

    if (!sortAscDirection) courses = courses.reverse

    for(SkipTake(skip, take) <- skipTake)
      courses = courses.slice(skip, skip + take)

    RangeResult(total, courses)
  }

  def getByCompanyId(companyId: Long, skipCheckActive: Boolean = false): Seq[LGroup] = {
    groupService
      .getCompanyGroups(companyId, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .asScala
      .filter(x => x.isSite && (skipCheckActive || x.isActive) && x.getFriendlyURL != "/control_panel")
  }

  def getById(courseId: Long) = {
    Option(groupService.fetchGroup(courseId))
  }

  def getGroupIdsForAllCoursesFromAllCompanies: Seq[Long] = {
    groupService.getGroupIdsForAllActiveSites
  }

  def getByUserId(userId: Long) = {
    groupService.getGroupsByUserId(userId).asScala
  }

  def getSitesByUserId(userId: Long): Seq[LGroup] = {
    groupService.getUserSitesGroups(userId).asScala
      .filter(notPersonalSite)
      .filter(isVisible)
  }

  def getSitesByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true): RangeResult[LGroup] = {
    val groups = getSitesByUserId(userId)
    val result = getSortedAndOrdered(groups, skipTake, sortAsc)

    RangeResult (
      groups.size,
      result
    )
  }

  def getByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean = true) = {
    val courses =
      if(skipTake.isDefined)
        groupService.getGroupsByUserId(userId, skipTake.get.skip, skipTake.get.take + skipTake.get.skip, sortAsc).asScala
      else
        groupService.getGroupsByUserId(userId).asScala

    val total = groupService.getGroupsCountByUserId(userId)

    RangeResult (
        total,
        courses
      )
  }

  def getByUserAndName(user: LUser,
                       skipTake: Option[SkipTake],
                       namePattern: Option[String],
                       sortAsc: Boolean): RangeResult[LGroup] = {

    val namePatternLC = namePattern.getOrElse("").toLowerCase

    val userOrganizations = user.getOrganizations
    val organizationGroups = userOrganizations.asScala.map(_.getGroup)


    val groups = (GroupLocalServiceHelper.getSiteGroupsByUser(user).filter(isMember(_,user)) ++ organizationGroups)
      .filter(namePatternFits(_,namePatternLC))
      .filter(hasCorrectType)
      .filter(notGuestSite)
      .filter(notPersonalSite)
      .filter(isVisible)

    val result = getSortedAndOrdered(groups, skipTake, sortAsc)

    RangeResult (
      groups.size,
      result
    )
  }

  private def getSortedAndOrdered(courses: Seq[LGroup], skipTake: Option[SkipTake], sortAsc : Boolean = true) = {
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

  def addCourse(companyId: Long,
                userId: Long,
                title: String,
                description:Option[String],
                friendlyUrl:Option[String],
                membershipType: CourseMembershipType,
                isActive: Boolean,
                tags: Seq[String]
               ): LGroup = {
    val course = GroupLocalServiceHelper.addPublicSite(
      userId,
      title,
      description,
      friendlyUrl,
      membershipType.id,
      isActive,
      tags)

    updateTags(companyId,course,tags)

    course
  }

  override def delete(courseId: Long): Unit = groupService.deleteGroup(courseId)

  override def rateCourse(courseId:Long, userId: Long, score:Double) = {
    ratingService.updateRating(userId, score, courseId)
    getRating(courseId, userId)
  }

  override def deleteCourseRating(courseId:Long, userId: Long) = {
    ratingService.deleteRating(userId, courseId)
    getRating(courseId, userId)
  }

  override def getRating(courseId: Long, userId: Long): Rating = {
    ratingService.getRating(userId, courseId)
  }

  def getCompanyIds: Seq[Long] = CompanyLocalServiceHelper.getCompanies.asScala.map(_.getCompanyId)

  override def getLogoUrl(courseId: Long) = {
    val layoutSet = LayoutSetLocalServiceHelper.getLayoutSet(courseId,true)
    if(layoutSet.isLogo) "/image/layout_set_logo?img_id=" + layoutSet.getLogoId
    else ""
  }

  override def setLogo(courseId: Long, content: Array[Byte]) = {
    LayoutSetLocalServiceHelper.updateLogo(courseId = courseId, privateLayout = true, logo = true, content = content)
    LayoutSetLocalServiceHelper.updateLogo(courseId = courseId, privateLayout = false, logo = true, content = content)
  }

  override def hasLogo(courseId: Long): Boolean = LayoutSetLocalServiceHelper.getLayoutSet(courseId,true).isLogo

  override def update(courseId: Long,
                      companyId: Long,
                      title: String,
                      description:Option[String],
                      friendlyUrl:Option[String],
                      membershipType: Option[CourseMembershipType],
                      isActive: Option[Boolean],
                      tags: Seq[String]
                     ): LGroup = {
    val originalGroup = groupService.getGroup(courseId)

    originalGroup.setName(title)
    originalGroup.setDescription(description.getOrElse(""))
    originalGroup.setFriendlyURL(friendlyUrl.getOrElse(originalGroup.getFriendlyURL))
    if(membershipType.isDefined) originalGroup.setType(membershipType.get.id)
    originalGroup.setActive(isActive.getOrElse(originalGroup.isActive))

    updateTags(companyId,originalGroup,tags)

    groupService.updateGroup(originalGroup)
  }

  private def updateTags(companyId: Long, course: LGroup, tags: Seq[String]): Unit = {
    val tagIds = courseTagService.getOrCreateTagIds(tags, companyId)
    categoryService.getCourseEntryIds(course.getGroupId)
      .foreach(courseTagService.setTags(_, tagIds))
  }

  override def getTags(courseId: Long): Seq[LAssetCategory] = categoryService.getCourseCategories(courseId)
}