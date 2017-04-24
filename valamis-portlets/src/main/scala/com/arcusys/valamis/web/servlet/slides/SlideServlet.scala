package com.arcusys.valamis.web.servlet.slides

import java.text.Normalizer
import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.slide.model.Slide
import com.arcusys.valamis.slide.service.SlideService
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.web.portlet.base.ViewPermission
import PortletName.LessonStudio
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.file.FileUploading
import com.arcusys.valamis.web.servlet.slides.request.SlideRequest
import com.arcusys.valamis.web.servlet.slides.response.SlideConverter._

class SlideServlet extends BaseApiController with FileUploading {

  private lazy val slideService = inject[SlideService]
  private lazy val uriService = inject[TincanURIService]

  private val req = SlideRequest(this)
  private def bgImage = Some(Normalizer.normalize(req.bgImage.getOrElse(""), Normalizer.Form.NFC))
  private def verbUUID = req.statementVerb.map { verbId =>
    val verbName = verbId.substring(verbId.lastIndexOf("/") + 1)
    if (TinCanVerbs.all.contains(verbName))
      verbId
    else
      uriService.getOrCreate(uriService.getLocalURL(), verbId, TincanURIType.Verb, Some(verbName)).objId
  }

  private def categoryUUID = req.statementCategoryId.map { categoryId =>
    uriService.getOrCreate(uriService.getLocalURL(), categoryId, TincanURIType.Category, Some(categoryId)).objId
  }

  get("/slides(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val slideList = if (req.isTemplate)
      slideService.getTemplateSlides
    else
      slideService.getSlides(req.slideSetIdOption.get)


    slideList.map(_.convertSlideModel)
  })

  get("/slides/:id/logo") {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val content = slideService.getBgImage(req.id) getOrElse {
      halt(HttpServletResponse.SC_NOT_FOUND, s"Slide with id: ${req.id} doesn't exist")
    }

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
  }

  post("/slides/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideService.update(
      Slide(
        id = req.id,
        title = req.title,
        bgColor = req.bgColor,
        font = req.font,
        questionFont = req.questionFont,
        answerFont = req.answerFont,
        answerBg = req.answerBg,
        duration = req.duration,
        leftSlideId = req.leftSlideId,
        topSlideId = req.topSlideId,
        slideSetId = req.slideSetId,
        statementVerb = verbUUID,
        statementObject = req.statementObject,
        statementCategoryId = categoryUUID,
        isTemplate = req.isTemplate,
        isLessonSummary = req.isLessonSummary,
        playerTitle = req.playerTitleOption,
        properties = req.slideProperties
      )
    ).convertSlideModel
  })

  post("/slides(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideService.create(
      Slide(
        title = req.title,
        bgColor = req.bgColor,
        font = req.font,
        questionFont = req.questionFont,
        answerFont = req.answerFont,
        answerBg = req.answerBg,
        duration = req.duration,
        leftSlideId = req.leftSlideId,
        topSlideId = req.topSlideId,
        slideSetId = req.slideSetId,
        statementVerb = verbUUID,
        statementObject = req.statementObject,
        statementCategoryId = categoryUUID,
        isTemplate = req.isTemplate,
        isLessonSummary = req.isLessonSummary,
        playerTitle = req.playerTitleOption,
        properties = req.slideProperties
      )
    ).convertSlideModel
  })

  post("/slides/:id/change-bg-image(/)")(jsonAction {
        PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
        slideService.updateBgImage(req.id, req.bgImage)
  })

  delete("/slides/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)
    slideService.delete(req.id)
  })
}
