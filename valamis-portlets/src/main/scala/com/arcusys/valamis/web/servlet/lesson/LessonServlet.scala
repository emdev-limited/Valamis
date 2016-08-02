package com.arcusys.valamis.web.servlet.lesson

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.constants.QueryUtilHelper
import com.arcusys.learn.liferay.services.GroupLocalServiceHelper
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.scorm.service.ActivityServiceContract
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.util.serialization.JsonHelper.StringOpts
import com.arcusys.valamis.web.portlet.util.PlayerPortletPreferences
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.servlet.lesson.serializer.LessonInfoSerializer
import com.arcusys.valamis.web.servlet.user.UserResponse
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.servlet.ServletBase

class LessonServlet
  extends BaseJsonApiController
    with PackagePolicy
    with ServletBase {

  lazy val req = LessonRequest(this)
  lazy val tagService = inject[TagService[Lesson]]

  lazy val lessonService = inject[LessonService]
  lazy val lessonViewersService = inject[LessonMembersService]
  lazy val lessonRatingService = inject[RatingService[Lesson]]
  lazy val lessonPlayerService = inject[LessonPlayerService]
  lazy val lessonLimitService = inject[LessonLimitService]
  lazy val lessonAttemptService = inject[UserLessonResultService]
  lazy val courseService = inject[CourseService]
  lazy val scormActivityService = inject[ActivityServiceContract]

  implicit override val jsonFormats: Formats =
    DefaultFormats + new LessonInfoSerializer + new EnumNameSerializer(LessonType) + DateTimeSerializer

  get("/packages(/)", request.getParameter("action") == "VISIBLE") {
    def getSuspendedId(userId: Long, lesson: Lesson): Option[String] = {
      lesson.lessonType match {
        case LessonType.Scorm   => scormActivityService.getSuspendedId(userId, lesson.id)
        case LessonType.Tincan  => None
      }
    }

    val companyId = req.companyId
    val courseId = req.courseIdRequired
    val playerId = req.playerId
    val user = getUser
    val tagId = req.tagId
    val titleFilter = req.textFilter

    lessonPlayerService.getForPlayer(courseId, playerId, user, titleFilter, tagId, req.ascending, req.sortBy, req.skipTake, getSuspendedId)
  }

  //TODO: rename action
  get("/packages(/)", request.getParameter("action") == "ALL_FOR_PLAYER") {
    lessonPlayerService.getAll(
      req.playerId,
      req.courseIdRequired
    )
  }

  get("/packages(/)", request.getParameter("action") == "ALL_AVAILABLE_FOR_PLAYER") {
    val courseId = req.courseIdRequired
    val sourceCourseIds = GroupLocalServiceHelper
      .searchExceptPrivateSites(getCompanyId, QueryUtilHelper.ALL_POS, QueryUtilHelper.ALL_POS)
      .map(i => i.getGroupId)
      .filterNot(_ == courseId)

    val filter = LessonFilter(
      Nil,
      req.lessonType,
      onlyVisible = true,
      req.textFilter,
      req.tagId
    )

    lessonPlayerService.getAvailableToAdd(
      req.playerId,
      req.courseIdRequired,
      sourceCourseIds,
      filter,
      req.ascending,
      req.skipTake
    )
  }

  get("/packages(/)", request.getParameter("action") == "ALL") {
    val courseIds = if(req.instanceScope) {
      GroupLocalServiceHelper.searchIdsExceptPrivateSites(req.companyId)
    } else {
      Seq(req.courseIdRequired)
    }

    val filter = LessonFilter(
      courseIds,
      req.lessonType,
      title = req.textFilter,
      tagId = req.tagId
    )

    lessonService.getAll(
      filter,
      req.ascending,
      req.skipTake
    )
  }


  get("/packages/:id/logo") {

    val content = lessonService.getLogo(req.id)
      .getOrElse(halt(HttpServletResponse.SC_NOT_FOUND, s"Package with id: ${req.id} doesn't exist"))

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    content
  }

  post("/packages(/)", request.getParameter("action") == "ADD_LESSONS_TO_PLAYER") {
    val lessonIds = req.ids
    val playerId = req.playerId

    lessonPlayerService.addLessonsToPlayer(playerId, lessonIds)
  }

  post("/packages(/)", request.getParameter("action") == "DELETE_LESSON_FROM_PLAYER") {
    val lessonId = req.id
    val playerId = req.playerId

    lessonPlayerService.deleteLessonFromPlayer(playerId, lessonId)
  }

  post("/packages(/)", request.getParameter("action") == "SET_PLAYER_DEFAULT") {
    val lessonId = req.idOption
    val playerId = req.playerId

    PlayerPortletPreferences(playerId, getCompanyId)
      .setDefaultLessonId(lessonId)
  }

  post("/packages(/)", request.getParameter("action") == "UPDATE") {
    val lessonId = req.id
    val title = req.title
    val description = req.description.getOrElse("")
    val isVisible = req.isVisible
    val beginDate = req.beginDate
    val endDate = req.endDate

    val limit = new LessonLimit(
      lessonId,
      req.passingLimit,
      req.rerunInterval,
      req.rerunIntervalType
    )

    val tags = tagService.getOrCreateTagIds(req.tags, getCompanyId)
    lessonLimitService.setLimit(limit)
    lessonService.update(lessonId, title, description, isVisible, beginDate, endDate, tags, req.requiredReview, req.scoreLimit)
  }

  post("/packages(/)", request.getParameter("action") == "UPDATE_VISIBLE") {
    lessonService.updateVisibility(req.id, req.isVisible)
  }

  post("/packages(/)", request.getParameter("action") == "ADD_MEMBERS") {
    lessonViewersService.addMembers(req.id, req.viewerIds, req.viewerType)
  }

  post("/packages(/)", request.getParameter("action") == "REMOVE_MEMBERS") {
    lessonViewersService.removeMembers(req.id, req.viewerIds, req.viewerType)
  }

  get("/packages(/)", request.getParameter("action") == "MEMBERS") {
    req.viewerType match  {
      case MemberTypes.User =>
        lessonViewersService.getUserMembers(req.id, req.textFilter, req.ascending, req.skipTake, req.organizationId)
          .map(u => new UserResponse(u))
      case _ =>
        lessonViewersService.getMembers(req.id, req.viewerType, req.textFilter, req.ascending, req.skipTake)
    }
  }

  get("/packages(/)", request.getParameter("action") == "AVAILABLE_MEMBERS") {
    req.viewerType match  {
      case MemberTypes.User =>
        lessonViewersService.getAvailableUserMembers(req.id, req.textFilter, req.ascending, req.skipTake, req.organizationId)
          .map(u => new UserResponse(u))
      case _ =>
        lessonViewersService.getAvailableMembers(req.id, req.viewerType, req.textFilter, req.ascending, req.skipTake)
    }
  }

  post("/packages(/)", request.getParameter("action") == "UPDATEPACKAGES") {
    val lessonsInfo = req.packages.parseTo[Seq[LessonInfo]]

    lessonService.updateLessonsInfo(lessonsInfo)
  }

  delete("/packages/:id(/)") {
    lessonService.delete(req.id)
  }

  post("/packages(/)", request.getParameter("action") == "REMOVEPACKAGES") {
    req.lessonIds foreach lessonService.delete
  }

  //TODO: move part to player
  get("/packages/tags(/)") {
    val playerId = req.playerIdOption
    val courseId = req.courseId

    if (playerId.isDefined && courseId.isDefined) {
      lessonPlayerService.getTagsFromPlayer(playerId.get, courseId.get)
    } else if (courseId.isDefined) {
      lessonService.getTagsFromCourse(courseId.get)
    } else {
      val courseIds = GroupLocalServiceHelper.searchIdsExceptPrivateSites(req.companyId)
      lessonService.getTagsFromCourses(courseIds)
    }
  }

  post("/packages/rate(/)", request.getParameter("action") == "UPDATERATING") {
    lessonRatingService.updateRating(getUserId, req.ratingScore, req.id)
  }

  post("/packages/rate(/)", request.getParameter("action") == "DELETERATING") {
    lessonRatingService.deleteRating(getUserId, req.id)
  }

  post("/packages/order(/)") {
    lessonPlayerService.updateOrder(req.playerId, req.lessonIds)
  }
}
