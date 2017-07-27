package com.arcusys.valamis.web.servlet.lesson

import javax.servlet.http.HttpServletResponse

import com.arcusys.valamis.lesson.service.LessonPlayerService
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import org.scalatra.servlet.ServletBase

import scala.util.Try

class LessonsSettingsServlet extends BaseJsonApiController
  with LessonSettingsPolicy
  with ServletBase {

  lazy val lessonPlayerService: LessonPlayerService = inject[LessonPlayerService]

  def playerId: Long = Try(params.as[Long]("playerId"))
    .getOrElse(halt(HttpServletResponse.SC_BAD_REQUEST, "Incorrect playerId"))

  get("/lessons-settings/categories(/)") {
    lessonPlayerService.getCategories(playerId)
  }

  post("/lessons-settings/categories(/)") {
    val categoriesIds = Try(multiParams.getOrElse("categoriesIds", Seq()).map(_.toLong))
      .getOrElse(halt(HttpServletResponse.SC_BAD_REQUEST, "Incorrect categoriesIds"))

    def courseId: Long = Try(params.as[Long]("courseId"))
      .getOrElse(halt(HttpServletResponse.SC_BAD_REQUEST, "Incorrect courseId"))

    lessonPlayerService.updateCategories(categoriesIds, playerId, courseId, getUserId)
  }

}
