package com.arcusys.valamis.web.servlet.public

import java.io.InputStream
import java.util.Locale
import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lesson.model.{Lesson, LessonLimit, LessonType}
import com.arcusys.valamis.lesson.service.export.PackageExportProcessor
import com.arcusys.valamis.lesson.service.{LessonLimitService, LessonMembersService, LessonService}
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.servlet.file.request.FileExportRequest
import com.arcusys.valamis.web.servlet.public.model.request.{LessonUpdateRequest, MemberRequest}
import com.arcusys.valamis.web.servlet.public.model.response.VisibilityType.VisibilityType
import com.arcusys.valamis.web.servlet.public.model.response._
import com.arcusys.valamis.web.servlet.public.parameters.LessonParameters
import com.arcusys.valamis.web.servlet.public.policy.LessonPolicy
import org.joda.time.DateTime
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer, PeriodSerializer}
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success}

/**
  * Created by pkornilov on 1/30/17.
  */
class LessonServlet extends BaseJsonApiController
  with LessonParameters
  with LessonPolicy {

  implicit override val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(LessonType) + new EnumNameSerializer(VisibilityType) +
      new EnumNameSerializer(MemberTypes) + DateTimeSerializer + PeriodSerializer

  protected lazy val lessonService = inject[LessonService]
  private lazy val lessonLimitService = inject[LessonLimitService]
  private lazy val lessonViewersService = inject[LessonMembersService]
  protected lazy val tagService = inject[TagService[Lesson]]
  private lazy val lessonRatingService = inject[RatingService[Lesson]]
  private lazy val packageExportProcessor = inject[PackageExportProcessor]

  implicit def locale: Locale = request.getLocale

  def getLessonLinkBasePath: String = {
    PortalUtilHelper.getPathMain
  }

  //lessons
  get("/lessons(/)") {
    lessonService.getLessonsForPublicApi(courseId, lessonType, skipTake) map toLessonResponse
  }

  get("/lessons/:id(/)") {
    lessonService.getLessonForPublicApi(id) map toLessonResponse getOrElse {
      haltWithLessonNotFound
    }
  }

  get("/lessons/:id/package") {
    lessonService.getLesson(id) map { lesson =>
      val stream = packageExportProcessor.exportItem(lesson.id)
      getZipStream(stream, lesson.id, lesson.creationDate)
    } getOrElse {
      haltWithLessonNotFound
    }
  }

  put("/lessons/:id(/)")(withLessonExistenceCheck(id) {
    val lesson = parsedBody.extract[LessonUpdateRequest]
    lessonLimitService.setLimit(toLessonLimit(lesson.rerunLimits))
    new LessonResponse(
      lesson =
        lessonService.update(id, lesson.title, lesson.description, fromVisibilityType(lesson.visibility),
          lesson.beginDate, lesson.endDate, lesson.requiredReview, lesson.scoreLimit),
      link = getLessonLink(id),
      limits = lesson.rerunLimits
    )
  })

  delete("/lessons/:id(/)")(withLessonExistenceCheck(id) {
    lessonService.delete(id)
  })

  //members
  get("/lessons/:id/members(/)")(withLessonExistenceCheck(id) {
    (memberType match {
      case MemberTypes.User =>
        lessonViewersService.getUserMembers(id, None, ascending = true, None, None) map {
          new MemberResponse(_)
        }
      case _ =>
        lessonViewersService.getMembers(id, memberType, None, ascending = true, None) map {
          new MemberResponse(_, memberType)
        }
    }).records
  })

  post("/lessons/:id/members(/)")(withLessonExistenceCheck(id) {
    val members = parsedBody.extract[Seq[MemberRequest]]
    members.groupBy(_.memberType).foreach { case (tpe, members) =>
      lessonViewersService.addMembers(id, members.map(_.id), tpe)
    }
  })

  delete("/lessons/:id/members(/)")(withLessonExistenceCheck(id) {
    val members = parsedBody.extract[Seq[MemberRequest]]
    members.groupBy(_.memberType).foreach { case (tpe, members) =>
      lessonViewersService.removeMembers(id, members.map(_.id), tpe)
    }
  })

  get("/lessons/:id/available-members(/)")(withLessonExistenceCheck(id) {
    (memberType match {
      case MemberTypes.User =>
        lessonViewersService.getAvailableUserMembers(id, None, ascending = true, None, None) map {
          new MemberResponse(_)
        }
      case _ =>
        lessonViewersService.getAvailableMembers(id, memberType, None, ascending = true, None) map {
          new MemberResponse(_, memberType)
        }
    }).records
  })

  //tags
  get("/lessons/:id/tags(/)")(withLessonExistenceCheck(id) {
    tagService.getByItemId(id)
  })

  put("/lessons/:id/tags(/)")(withLessonExistenceCheck(id) {
    val tagNames = (parsedBody \\ "tags").extract[Seq[String]]
    val tagIds = tagService.getOrCreateTagIds(tagNames, getCompanyId)
    lessonService.updateLessonTags(id, tagIds)
  })

  //ratings
  get("/lessons/:id/rating(/)")(withLessonExistenceCheck(id) {
    lessonRatingService.getRating(getUserId, id)
  })

  put("/lessons/:id/rating(/)")(withLessonExistenceCheck(id) {
    lessonRatingService.updateRating(getUserId, ratingScore, id)
  })

  delete("/lessons/:id/rating(/)")(withLessonExistenceCheck(id){
    lessonRatingService.deleteRating(getUserId, id)
  })

  //logo
  get("/lessons/:id/logo(/)")(withLessonExistenceCheck(id) {
    val content = lessonService.getLogo(id).getOrElse {
      haltWithNotFound(s"There is no logo for lesson with id $id")
    }

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    content
  })

  private def withLessonExistenceCheck[T](lessonId: Long)(action: => T) = {
    if (lessonService.isExisted(lessonId)) {
      action
    } else {
      haltWithLessonNotFound
    }
  }

  private def toLessonResponse(data: (Lesson, Option[LessonLimit])) = {
    val (lesson, limit) = data

    val rerunLimits = limit map { l =>
      RerunLimits(
        maxAttempts = l.passingLimit,
        period = l.rerunInterval flatMap { interval =>
          PeriodTypes.toJodaPeriod(l.rerunIntervalType, interval)
        }
      )
    }

    new LessonResponse(lesson, getLessonLink(lesson.id), rerunLimits)
  }

  private def getLessonLink(id: Long): String =
    s"$getLessonLinkBasePath/portal/learn-portlet/open_package?oid=$id"

  private def fromVisibilityType(visibilityType: VisibilityType): Option[Boolean] = {
    visibilityType match {
      case VisibilityType.Hidden => Some(false)
      case VisibilityType.Public => Some(true)
      case VisibilityType.Custom => None
    }
  }

  private def toLessonLimit(rerunLimits: Option[RerunLimits]): LessonLimit = {
    rerunLimits match {
      case None => LessonLimit(
        lessonId = id,
        passingLimit = None,
        rerunInterval = None
      )
      case Some(RerunLimits(maxAttempts, period)) =>
        val (intervalType, interval) = PeriodTypes.fromJodaPeriod(period) match {
          case Failure(ex) => haltWithBadRequest(ex)
          case Success(v) => v
        }

        LessonLimit(
          lessonId = id,
          passingLimit = maxAttempts,
          rerunInterval = interval,
          rerunIntervalType = intervalType
        )
    }
  }

  private def haltWithLessonNotFound = haltWithNotFound(s"Lesson with id $id doesn't exist")

  private def getZipStream(fileStream: InputStream, id: Long, creationDate: DateTime) = {
    response.setHeader("Content-Type", "application/zip")
    response.setHeader("Content-Disposition", s"attachment; filename=package_${id}_${creationDate.toString("YYYY-MM-dd_HH-mm-ss")}${FileExportRequest.ExportExtension}")
    fileStream
  }

}
