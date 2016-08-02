package com.arcusys.valamis.web.servlet.slides

import java.text.Normalizer
import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.slide.exeptions.NoSlideElementException
import com.arcusys.valamis.slide.model.{SlideElementModel, SlideEntityType}
import com.arcusys.valamis.slide.service.SlideElementServiceContract
import com.arcusys.valamis.web.portlet.base.ViewPermission
import PortletName._
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.file.FileUploading
import com.arcusys.valamis.web.servlet.slides.request.{SlideActionType, SlideRequest}
import com.arcusys.valamis.web.servlet.slides.response.SlideElementConverter._

class SlideElementServlet extends BaseApiController with FileUploading {

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
    lazy val content = Normalizer.normalize(slideRequest.content,Normalizer.Form.NFC)
    slideRequest.action match {
      case SlideActionType.Delete => slideElementService.delete(slideRequest.id.get)
      case SlideActionType.Update =>
        haltOnInvalidData(slideRequest)
        slideElementService.update(
          SlideElementModel(
            slideRequest.id,
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
        //TODO: remove api for clone later
      case SlideActionType.Clone =>
        haltOnInvalidData(slideRequest)
        try {
          val slideElement = slideElementService.getById(slideRequest.idRequired)
            .getOrElse(throw new NoSlideElementException(slideRequest.idRequired))
          slideElementService.clone(
            slideElement,
            slideRequest.slideId,
            isTemplate = false
          ).convertSlideElementModel()
        } catch {
          case e: NoSlideElementException =>
            halt(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
        }
    }
  })
}