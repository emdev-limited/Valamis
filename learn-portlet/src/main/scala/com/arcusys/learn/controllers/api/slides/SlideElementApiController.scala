package com.arcusys.learn.controllers.api.slides

import java.text.Normalizer
import javax.servlet.http.HttpServletResponse
import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.PortletName.LessonStudio
import com.arcusys.learn.liferay.permission.{PermissionUtil, ViewPermission}
import com.arcusys.learn.models.request.{SlideActionType, SlideRequest}
import com.arcusys.learn.web.FileUploading
import com.arcusys.valamis.slide.exeptions.NoSlideElementException
import com.arcusys.valamis.slide.model.{SlideElementModel, SlideEntityType}
import com.arcusys.valamis.slide.service.SlideElementServiceContract
import com.arcusys.learn.models.response.SlideElementConverter._

class SlideElementApiController extends BaseApiController with FileUploading {

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  private lazy val slideElementService = inject[SlideElementServiceContract]

  def haltOnInvalidData(slideRequest: SlideRequest.Model) {
    if (!SlideEntityType.AvailableTypes.contains(slideRequest.slideEntityType)) halt(HttpServletResponse.SC_BAD_REQUEST, "Unknown slide entity type")
  }

  private val slideRequest = SlideRequest(this)

  get("/slideentities(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    slideElementService.getBySlideId(slideRequest.slideId)
  })

  get("/slideentities/:id/logo")(action {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val content = slideElementService.getLogo(slideRequest.id.get)
      .getOrElse( halt(HttpServletResponse.SC_NOT_FOUND, s"SlideEntity with id: ${slideRequest.id.get} doesn't exist") )

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
  })

  post("/slideentities(/)(:id)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    //if DB filenames always stored in NFC (Unicode Normalization Form C)
    //but filenames, came from frontend might be in NFD (Unicode Normalization Form D)
    //so we have to convert it from NFD to NFC (didn't do this in js, because of normalize function only supported in ECMAScript 2015(ES6))
    //https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/String/normalize
    val content = Normalizer.normalize(slideRequest.content,Normalizer.Form.NFC)
    slideRequest.action match {
      case SlideActionType.Delete => slideElementService.delete(slideRequest.id.get)
      case SlideActionType.Update =>
        haltOnInvalidData(slideRequest)
        slideElementService.update(
          SlideElementModel(
            slideRequest.id,
            slideRequest.top,
            slideRequest.left,
            slideRequest.width,
            slideRequest.height,
            slideRequest.zIndex,
            content,
            slideRequest.slideEntityType,
            slideRequest.slideId,
            slideRequest.correctLinkedSlideId,
            slideRequest.incorrectLinkedSlideId,
            slideRequest.notifyCorrectAnswer,
            slideRequest.properties
          )
        ).convertSlideElementModel()
      case SlideActionType.Create =>
        haltOnInvalidData(slideRequest)
        slideElementService.create(
          SlideElementModel(
            None,
            slideRequest.top,
            slideRequest.left,
            slideRequest.width,
            slideRequest.height,
            slideRequest.zIndex,
            content,
            slideRequest.slideEntityType,
            slideRequest.slideId,
            slideRequest.correctLinkedSlideId,
            slideRequest.incorrectLinkedSlideId,
            slideRequest.notifyCorrectAnswer,
            slideRequest.properties
          )
        ).convertSlideElementModel()
      case SlideActionType.Clone =>
        haltOnInvalidData(slideRequest)
        try {
          slideElementService.clone(
            slideRequest.id.get,
            slideRequest.slideId,
            slideRequest.correctLinkedSlideId,
            slideRequest.incorrectLinkedSlideId,
            slideRequest.width,
            slideRequest.height,
            content,
            slideRequest.isTemplate.getOrElse(false),
            slideRequest.properties
          ).convertSlideElementModel()
        } catch {
          case e: NoSlideElementException =>
            halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
        }
    }
  })
}