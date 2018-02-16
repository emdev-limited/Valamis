package com.arcusys.valamis.web.servlet.slides

import java.text.Normalizer
import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.slide.exeptions.NoSlideElementException
import com.arcusys.valamis.slide.model.{SlideElement, SlideEntityType}
import com.arcusys.valamis.slide.service.SlideElementService
import com.arcusys.valamis.web.portlet.base.ViewPermission
import PortletName._
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.file.FileUploading
import com.arcusys.valamis.web.servlet.slides.request.SlideRequest
import com.arcusys.valamis.web.servlet.slides.response.SlideElementConverter._

class SlideElementServlet extends BaseApiController with FileUploading {

  private lazy val slideElementService = inject[SlideElementService]
  private lazy val lineBreakStr = "[\u2028\u2029]+"
  //if DB filenames always stored in NFC (Unicode Normalization Form C)
  //but filenames, came from frontend might be in NFD (Unicode Normalization Form D)
  //so we have to convert it from NFD to NFC (didn't do this in js, because of normalize function only supported in ECMAScript 2015(ES6))
  //https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/String/normalize
  private def contentNFC = Normalizer.normalize(req.content, Normalizer.Form.NFC)
  //in text element's content \u2028 and \u2029(line/paragraph separators) characters can break JSON
  //we need correct JSON for export and publish slideSet
  private def content =
    if (req.slideEntityType == SlideEntityType.Text)
      contentNFC.replaceAll(lineBreakStr, "")
    else contentNFC

  def haltOnInvalidData(slideRequest: SlideRequest.Model) {
    if (!SlideEntityType.AvailableTypes.contains(slideRequest.slideEntityType)) halt(HttpServletResponse.SC_BAD_REQUEST, "Unknown slide entity type")
  }

  private val req = SlideRequest(this)

  get("/slide-elements(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideElementService.getBySlideId(req.slideId)
  })

  get("/slide-elements/:id/logo") {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val content = slideElementService.getLogo(req.id)
      .getOrElse( halt(HttpServletResponse.SC_NOT_FOUND, s"SlideElement with id: ${req.id} doesn't exist") )

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
  }

  post("/slide-elements/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    haltOnInvalidData(req)
    slideElementService.update(
      SlideElement(
        req.id,
        req.zIndex,
        content,
        req.slideEntityType,
        req.slideId,
        req.correctLinkedSlideId,
        req.incorrectLinkedSlideId,
        req.notifyCorrectAnswer,
        req.properties
      )
    ).convertSlideElementModel
  })

  post("/slide-elements(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    haltOnInvalidData(req)
    slideElementService.create(
      SlideElement(
        zIndex = req.zIndex,
        content = content,
        slideEntityType = req.slideEntityType,
        slideId = req.slideId,
        correctLinkedSlideId = req.correctLinkedSlideId,
        incorrectLinkedSlideId = req.incorrectLinkedSlideId,
        notifyCorrectAnswer = req.notifyCorrectAnswer,
        properties = req.properties
      )
    ).convertSlideElementModel
  })

  delete("/slide-elements/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideElementService.delete(req.id)
  })
}