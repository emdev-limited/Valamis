package com.arcusys.valamis.web.servlet.slides

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.services.{PermissionHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName.LessonStudio
import com.arcusys.valamis.content.exceptions.NoContentException
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.slide.model.{SlideSetSort, SlideSet}
import com.arcusys.valamis.slide.service.{SlideService, SlideSetAssetHelper, SlideSetPublishService, SlideSetService}
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.file.FileUploading
import com.arcusys.valamis.web.servlet.slides.request.SlideRequest
import com.arcusys.valamis.web.servlet.slides.response.SlideSetResponse
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}

class SlideSetServlet extends BaseApiController with FileUploading {
  implicit val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

  private lazy val slideSetService = inject[SlideSetService]
  private lazy val slideService = inject[SlideService]
  private lazy val slideSetPublishService = inject[SlideSetPublishService]
  private lazy val slideAssetHelper = inject[SlideSetAssetHelper]
  private lazy val req = SlideRequest(this)

  get("/slide-sets(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val sortBy = SlideSetSort(req.sortBy, Order(req.ascending))
    slideSetService.getSlideSets(
        req.courseId,
        req.titleFilter,
        sortBy,
        req.skipTake,
        req.isTemplate)
      .map(slideSet => {
        val lockUser = slideSet.lockUserId.map(UserLocalServiceHelper().getUser)
        val tags = slideAssetHelper.getSlideAssetCategories(slideSet.id)
        val slidesCount = slideService.getCount(slideSet.id)

        new SlideSetResponse(slideSet, lockUser, Some(slidesCount), tags)
      })
  })

  get("/slide-sets/:id/logo") {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val content = slideSetService.getLogo(req.id)
      .getOrElse( halt(HttpServletResponse.SC_NOT_FOUND, s"SlideSet with id: ${req.id} doesn't exist") )

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
  }

  get("/slide-sets/:id/versions(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.getVersions(req.id)
  })

 
  post("/slide-sets/:id/change-lock-status(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.changeLockStatus(req.id, req.lockUserId)
  })

  get("/slide-sets/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    val slideSet = slideSetService.getById(req.id)
    val lockUser = slideSet.lockUserId.map(UserLocalServiceHelper().getUser)

    new SlideSetResponse(slideSet, lockUser)
  })

  //TODO: UI call this method before publish
  // we need to change it, we have no lesson before first publish
  post("/slide-sets/:id/lessonId")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetPublishService.findPublishedLesson(req.id, PermissionUtil.getUserId)
  })

  post("/slide-sets/:id/update-info(/)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    slideSetService.updateInfo(
      req.id,
      req.title,
      req.description,
      req.tags)

  })

  post("/slide-sets/:id(/)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    slideSetService.updateSettings(
        req.id,
        req.isSelectedContinuity,
        req.themeId,
        req.slideSetDuration,
        req.scoreLimit,
        req.playerTitle,
        req.topDownNavigation,
        req.oneAnswerAttempt,
        req.requiredReview)
  })

  post("/slide-sets(/)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.createWithDefaultSlide(
      SlideSet(
        title = req.title,
        description = req.description,
        courseId = req.courseId,
        logo = req.logo,
        isTemplate = req.isTemplate,
        isSelectedContinuity = req.isSelectedContinuity
      ),
      req.tags
    )
  })

  post("/slide-sets/:id/publish(/)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    try {
      slideSetPublishService.publish(
        getServletContext,
        req.id,
        userId,
        req.courseId
      )
    }
    catch {
      case e: NoContentException => halt(424, s"{relation:'${e.contentType}', id:${e.id}}", reason = e.getMessage)
    }
  })

  post("/slide-sets/:id/clone(/)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.clone(
      req.id,
      req.isTemplate,
      req.fromTemplate.getOrElse(false),
      req.title,
      req.description,
      req.logo,
      req.newVersion
    )
  })

  delete("/slide-sets/:id(/)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.delete(req.id)
  })

  delete("/slide-sets/:id/versions(/)")(jsonAction {
    val userId = PermissionUtil.getUserId
    PermissionHelper.preparePermissionChecker(userId)
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideSetService.deleteAllVersions(req.id)
  })
}