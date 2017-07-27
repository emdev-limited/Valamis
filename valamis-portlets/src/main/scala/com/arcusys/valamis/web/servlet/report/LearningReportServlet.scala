package com.arcusys.valamis.web.servlet.report

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.gradebook.service.TeacherCourseGradeService
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lesson.tincan.service.TincanPackageService
import com.arcusys.valamis.lrssupport.lrs.service.util.TinCanActivityType
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.reports.model.{PathsReportStatus, PatternReportStatus}
import com.arcusys.valamis.reports.service.LearningPatternReportService
import com.arcusys.valamis.user.model.{UserFilter, UserSort, UserSortBy}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.util._
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.certificate.response._
import com.arcusys.valamis.web.servlet.report.response.learningReport._
import com.arcusys.valamis.web.servlet.user.UserResponse
import org.json4s.ext.{EnumSerializer, JodaTimeSerializers}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{BadRequest, NotFound}

import scala.collection.JavaConverters._

//TODO: improve user reading
class LearningReportServlet extends BaseJsonApiController {

  lazy val lessonService = inject[LessonService]
  lazy val tincanLessonService = inject[TincanPackageService]
  lazy val userService = inject[UserService]
  lazy val courseGradeService = inject[TeacherCourseGradeService]

  lazy val patternReportService = inject[LearningPatternReportService]

  override protected implicit val jsonFormats: Formats = DefaultFormats +
    new EnumSerializer(PatternReportStatus) +
    new EnumSerializer(PathsGoalType) +
    new EnumSerializer(PathsReportStatus) ++
    JodaTimeSerializers.all

  private implicit def locale = UserLocalServiceHelper().getUser(PermissionUtil.getUserId).getLocale

  def companyId = PermissionUtil.getCompanyId

  def courseId: Long = params.getAs[Long]("courseId") getOrElse halt(NotFound())

  def courseIdOpt: Option[Long] = params.getAs[Long]("courseId").orElse {
    if (params("courseId") == "all") None else halt(NotFound())
  }

  def filter: Option[String] = params.get("filter").filterNot(_.isEmpty)

  get("/learning-pattern-report/course/:courseId/lessons(/)") {
    patternReportService.getLessons(courseId)
      .map { lesson =>
        new LessonResponse(
          lesson,
          hasQuestion = tincanLessonService.hasActivities(lesson.id, TinCanActivityType.cmiInteraction)
        )
      }
  }

  get("/learning-pattern-report/course/:courseId/usersCount(/)") {
    val usersCount = userService.getCountBy(UserFilter(
      companyId = Some(companyId),
      namePart = filter,
      groupId = Some(courseId)
    ))
    Map("result" -> usersCount)
  }

  get("/learning-pattern-report/course/:courseId/users(/)") {
    val skip = params.getAs[Int]("skip").getOrElse(0)
    val take = params.getAs[Int]("take").getOrElse(10)

    val users = getUsers(Some(courseId), filter, Some(SkipTake(skip, take)))

    val statuses = patternReportService.getStatuses(courseId, users)

    users.map { user =>
      UserLessonsResponse(
        id = user.getUserId,
        user = new UserResponse(user),
        grade = courseGradeService.get(courseId, user.getUserId).flatMap(_.grade),
        organizations = user.getOrganizations.asScala.map(_.getName),
        lessons = statuses.filter(_.userId == user.getUserId))
    }
  }

  get("/learning-pattern-report/course/:courseId/lessons/total(/)") {
    val users = getUsers(Some(courseId), filter)

    patternReportService.getStatusesTotal(courseId, users)
      .map { case (lesson, total) =>
        TotalResponse(lesson.id, total.map { case (k, v) => (k.id, v) })
      }
  }

  get("/learning-pattern-report/course/:courseId/lesson/:lessonId/slides(/)") {
    val lesson = params.getAs[Long]("lessonId")
      .flatMap(lessonService.getLesson)
      .getOrElse(halt(NotFound()))

    val users = multiParams.getAs[Long]("userIds").getOrElse(Nil)
      .map(userService.getById)

    val statuses = patternReportService.getLessonActivitiesStatuses(lesson.id, users)

    users.flatMap { user =>
      statuses.find(_.userId == user.getUserId)
        .map(new UserLessonSlidesResponse(user, lesson, _))
    }
  }

  get("/learning-pattern-report/course/:courseId/lesson/:lessonId/total(/)") {
    val lesson = params.getAs[Long]("lessonId")
      .flatMap(lessonService.getLesson)
      .getOrElse(halt(NotFound()))

    val users = getUsers(Some(courseId), filter)

    patternReportService.getLessonActivitiesStatusesTotal(lesson.id, users)
      .map { case (activity, total) =>
        TotalResponse(activity.id.get, total.map { case (k, v) => (k.id, v) })
      }
  }

  post("/learning-pattern-report/settings/lessons/:settingsId") {
    val settingsId = params.as[Long]("settingsId")
    val companyId = getCompanyId
    val courseId = PermissionUtil.getCourseId

    val settings = try {
      LearningReportLessonsSettings(
        params.as[Long]("reportCourseId"),
        PatternReportStatus(params.as[Int]("status")),
        params.as[Boolean]("questionOnly"),
        multiParams.getAs[Long]("lessonIds").getOrElse(Nil),
        multiParams.getAs[Long]("userIds").getOrElse(Nil),
        params.as[Boolean]("hasSummary")
      )
    }
    catch {
      case e: Exception => halt(BadRequest(e.getMessage))
    }

    new LearningReportSettingsService(settingsId, companyId, courseId)
      .set(settings)

    settings
  }

  post("/learning-pattern-report/settings/paths/:settingsId") {
    val settingsId = params.as[Long]("settingsId")
    val companyId = getCompanyId
    val courseId = PermissionUtil.getCourseId

    val settings = try {
      LearningReportPathsSettings(
        params.as[Long]("reportCourseId"),
        PathsReportStatus(params.as[Int]("status")),
        PathsGoalType(params.as[Int]("goalType")),
        multiParams.getAs[Long]("certificateIds").getOrElse(Nil),
        multiParams.getAs[Long]("userIds").getOrElse(Nil),
        params.as[Boolean]("hasSummary")
      )
    }
    catch {
      case e: Exception => halt(BadRequest(e.getMessage))
    }

    new LearningReportSettingsService(settingsId, companyId, courseId)
      .set(settings)

    settings
  }

  private def getUsers(groupId: Option[Long],
                       filter: Option[String],
                       skipTake: Option[SkipTake] = None) = {
    userService.getBy(
      UserFilter(
        companyId = Some(companyId),
        namePart = filter,
        groupId = groupId,
        sortBy = Some(UserSort(UserSortBy.Name, Order.Asc))
      ),
      skipTake
    )
  }

}
