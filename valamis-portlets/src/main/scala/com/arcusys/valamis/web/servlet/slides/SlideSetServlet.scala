package com.arcusys.valamis.web.servlet.slides

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.content.exceptions.NoContentException
import com.arcusys.valamis.slide.model.SlideSetModel
import com.arcusys.valamis.slide.service.SlideSetServiceContract
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.portlet.base.ViewPermission
import PortletName._
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.file.FileUploading
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import com.arcusys.valamis.web.servlet.slides.request.{SlideActionType, SlideRequest}
import org.joda.time.DateTime
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}

class SlideSetServlet extends BaseApiController with FileUploading {
  implicit val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

  private lazy val slideSetService = inject[SlideSetServiceContract]
  private lazy val resourceReader = inject[ResourceReader]

  private lazy val slideRequest = SlideRequest(this)

  get("/slidesets(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val page = slideRequest.page

    val requestedSlideSetsCount =
      slideSetService.getSlideSetsCount(
        slideRequest.titleFilter,
        slideRequest.courseId,
        slideRequest.isTemplate)
    val requestedSlideSets =
      slideSetService.getSlideSets(
        slideRequest.courseId,
        slideRequest.titleFilter,
        slideRequest.sortTitleAsc,
        slideRequest.skipTake,
        slideRequest.isTemplate)

    CollectionResponse(page, requestedSlideSets, requestedSlideSetsCount)
  })

  post("/slidesets/:id/lessonId", slideRequest.action == "prepareLesson")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    slideSetService.findSlideLesson(slideRequest.id.get, PermissionUtil.getUserId)
  })

  get("/slidesets/:id/logo")(action {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val content = slideSetService.getLogo(slideRequest.id.get)
      .getOrElse( halt(HttpServletResponse.SC_NOT_FOUND, s"SlideSet with id: ${slideRequest.id.get} doesn't exist") )

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
  })

  get("/slidesets/:id/versions(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.getVersions(slideRequest.id.get)
  })

  get("/slidesets/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.getById(slideRequest.id.get)
  })

  post("/slidesets(/)(:id)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    slideRequest.action match {
      case SlideActionType.Delete => slideSetService.delete(slideRequest.id.get)
      case SlideActionType.DeleteAllVersions => slideSetService.deleteAllVersions(slideRequest.id.get)
      case SlideActionType.Update => slideSetService.updateWithVersion(
        SlideSetModel(
          slideRequest.id,
          slideRequest.title,
          slideRequest.description,
          slideRequest.courseId,
          slideRequest.logo,
          List(),
          slideRequest.isTemplate.getOrElse(false),
          slideRequest.isSelectedContinuity.getOrElse(false),
          slideRequest.themeId,
          slideRequest.slideSetDuration,
          slideRequest.scoreLimit,
          slideRequest.playerTitle,
          None,
          slideRequest.topDownNavigation,
          slideRequest.activityId,
          slideRequest.status,
          slideRequest.version,
          new DateTime(),
          slideRequest.oneAnswerAttempt),
        slideRequest.tags
      )
      case SlideActionType.Create => slideSetService.createWithDefaultSlide(
        SlideSetModel(
          None,
          slideRequest.title,
          slideRequest.description,
          slideRequest.courseId,
          slideRequest.logo,
          List(),
          slideRequest.isTemplate.getOrElse(false),
          slideRequest.isSelectedContinuity.getOrElse(false)
        ),
        slideRequest.tags
      )
      case SlideActionType.Publish =>
        try {

          slideSetService.publishSlideSet(
            getServletContext,
            slideRequest.id.get,
            userId,
            slideRequest.courseId
          )
        }
        catch {
          case e: NoContentException => halt(424, s"{relation:'${e.contentType}', id:${e.id}}", reason = e.getMessage)
        }
      case SlideActionType.Clone => slideSetService.clone(
        slideRequest.id.get,
        slideRequest.isTemplate.getOrElse(false),
        slideRequest.fromTemplate.getOrElse(false),
        slideRequest.title,
        slideRequest.description,
        slideRequest.logo,
        slideRequest.newVersion
      )
      case _ => throw new UnsupportedOperationException("Unknown action type")
    }
  })
}