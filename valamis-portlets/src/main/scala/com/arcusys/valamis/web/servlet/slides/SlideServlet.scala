package com.arcusys.valamis.web.servlet.slides

import java.text.Normalizer
import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.slide.model.SlideModel
import com.arcusys.valamis.slide.service.SlideServiceContract
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.web.portlet.base.ViewPermission
import PortletName.LessonStudio
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.file.FileUploading
import com.arcusys.valamis.web.servlet.slides.request.{SlideActionType, SlideRequest}
import com.arcusys.valamis.web.servlet.slides.response.SlideConverter._

class SlideServlet extends BaseApiController with FileUploading {

  private lazy val slideService = inject[SlideServiceContract]
  private lazy val uriService = inject[TincanURIService]

  private val slideRequest = SlideRequest(this)

  get("/slides(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val slideSetId = slideRequest.slideSetIdOption

    val slideList = if (slideSetId.isDefined)
      slideService.getBySlideSetId(slideSetId.get, slideRequest.isTemplate)
    else
      slideService.getAll(slideRequest.isTemplate)

    slideList.map(slide => slide.convertSlideModel())
  })

  get("/slides/:id/logo")(action {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val content = slideService.getLogo(slideRequest.id.get)
      .getOrElse(halt(HttpServletResponse.SC_NOT_FOUND, s"Slide with id: ${slideRequest.id.get} doesn't exist"))

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
  })

  post("/slides(/)(:id)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val verbUUID = slideRequest.statementVerb.map { verbId =>
      val verbName = verbId.substring(verbId.lastIndexOf("/") + 1)
      if (TinCanVerbs.all.contains(verbName))
        verbId
      else
        uriService.getOrCreate(uriService.getLocalURL(), verbId, TincanURIType.Verb, Some(verbName)).objId
    }

    val categoryUUID = slideRequest.statementCategoryId.map { categoryId =>
      uriService.getOrCreate(uriService.getLocalURL(), categoryId, TincanURIType.Category, Some(categoryId)).objId
    }

    val bgImage = Some(Normalizer.normalize(slideRequest.bgImage.getOrElse(""), Normalizer.Form.NFC))

    slideRequest.action match {
      case SlideActionType.Delete => slideService.delete(slideRequest.id.get)
      case SlideActionType.Update => slideService.update(
        SlideModel(
          slideRequest.id,
          slideRequest.title,
          slideRequest.bgColor,
          bgImage,
          slideRequest.font,
          slideRequest.questionFont,
          slideRequest.answerFont,
          slideRequest.answerBg,
          slideRequest.duration,
          slideRequest.leftSlideId,
          slideRequest.topSlideId,
          List(),
          slideRequest.slideSetId,
          verbUUID,
          slideRequest.statementObject,
          categoryUUID,
          slideRequest.isTemplate.getOrElse(false),
          slideRequest.isLessonSummary.getOrElse(false),
          slideRequest.playerTitleOption,
          slideRequest.slideProperties
        )
      ).convertSlideModel()
      case SlideActionType.Create => slideService.create(
        SlideModel(
          None,
          slideRequest.title,
          slideRequest.bgColor,
          bgImage,
          slideRequest.font,
          slideRequest.questionFont,
          slideRequest.answerFont,
          slideRequest.answerBg,
          slideRequest.duration,
          slideRequest.leftSlideId,
          slideRequest.topSlideId,
          List(),
          slideRequest.slideSetId,
          verbUUID,
          slideRequest.statementObject,
          categoryUUID,
          slideRequest.isTemplate.getOrElse(false),
          slideRequest.isLessonSummary.getOrElse(false),
          slideRequest.playerTitleOption,
          slideRequest.slideProperties
        )
      ).convertSlideModel()
      case SlideActionType.CopyFile => slideService.copyFileFromTheme(
        slideRequest.id.get,
        slideRequest.themeId.get
      )
    }
  })
}
