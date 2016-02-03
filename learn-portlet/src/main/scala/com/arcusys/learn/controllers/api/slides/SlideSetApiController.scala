package com.arcusys.learn.controllers.api.slides

import javax.servlet.http.HttpServletResponse
import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.PortletName.LessonStudio
import com.arcusys.learn.liferay.permission.{PermissionUtil, ViewPermission}
import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.learn.models.request.{SlideActionType, SlideRequest}
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.learn.web.FileUploading
import com.arcusys.valamis.questionbank.exceptions.NoQuestionException
import com.arcusys.valamis.slide.model.SlideSetModel
import com.arcusys.valamis.slide.service.SlideSetServiceContract

class SlideSetApiController extends BaseApiController with FileUploading {

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  private lazy val slideSetService = inject[SlideSetServiceContract]

  private lazy val slideRequest = SlideRequest(this)

  get("/slidesets(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val page = slideRequest.page

    val requestedSlideSetsCount =
      slideSetService.getSlideSetsCount(
        slideRequest.titleFilter,
        slideRequest.courseIdOption,
        slideRequest.isTemplate)
    val requestedSlideSets =
      slideSetService.getSlideSets(
        slideRequest.titleFilter,
        slideRequest.sortTitleAsc,
        slideRequest.page,
        slideRequest.itemsOnPage,
        slideRequest.courseIdOption,
        slideRequest.isTemplate)

    CollectionResponse(page, requestedSlideSets, requestedSlideSetsCount)
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

  post("/slidesets(/)(:id)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val learnPortletPath = getServletContext.getRealPath("/")

    slideRequest.action match {
      case SlideActionType.Delete => slideSetService.delete(slideRequest.id.get)
      case SlideActionType.Update => slideSetService.update(
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
          slideRequest.playerTitle
        )
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
        )
      )
      case SlideActionType.Publish =>
        try {
          slideSetService.publishSlideSet(
            slideRequest.id.get,
            userId,
            learnPortletPath,
            slideRequest.courseId
          )
        }
        catch {
          case e: NoQuestionException => halt(424, s"{relation:'question', id:${e.id}}", reason = e.getMessage)
        }
      case SlideActionType.Clone => slideSetService.clone(
        slideRequest.id.get,
        slideRequest.isTemplate.getOrElse(false),
        slideRequest.fromTemplate.getOrElse(false),
        slideRequest.title,
        slideRequest.description,
        slideRequest.logo
      )
      case _ => throw new UnsupportedOperationException("Unknown action type")
    }
  })
}

