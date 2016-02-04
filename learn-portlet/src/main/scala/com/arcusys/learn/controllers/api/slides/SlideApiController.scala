package com.arcusys.learn.controllers.api.slides

import java.text.Normalizer
import javax.servlet.http.HttpServletResponse
import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.PortletName.LessonStudio
import com.arcusys.learn.liferay.permission.{PermissionUtil, ViewPermission}
import com.arcusys.learn.models.request.{SlideActionType, SlideRequest}
import com.arcusys.learn.web.FileUploading
import com.arcusys.valamis.lrs.util.TinCanVerbs
import com.arcusys.valamis.slide.model.SlideModel
import com.arcusys.valamis.slide.service.SlideServiceContract
import com.arcusys.valamis.uri.model.ValamisURIType
import com.arcusys.valamis.uri.service.URIServiceContract
import com.arcusys.learn.models.response.SlideConverter._

class SlideApiController extends BaseApiController with FileUploading {

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  private lazy val slideService = inject[SlideServiceContract]
  private lazy val uriService = inject[URIServiceContract]

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
        uriService.getOrCreate(uriService.getLocalURL(), verbId, ValamisURIType.Verb, Some(verbName)).objId
    }

    val categoryUUID = slideRequest.statementCategoryId.map { categoryId =>
      uriService.getOrCreate(uriService.getLocalURL(), categoryId, ValamisURIType.Category, Some(categoryId)).objId
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
      case SlideActionType.Clone => {
        val clonedSlide = slideService.clone(
          slideRequest.slideId,
          slideRequest.leftSlideId,
          slideRequest.topSlideId,
          bgImage,
          slideRequest.slideSetId,
          slideRequest.isTemplate.getOrElse(false),
          slideRequest.isLessonSummary.getOrElse(false),
          slideRequest.fromTemplate.getOrElse(false),
          slideRequest.cloneElements.getOrElse(true)
        )
        if (clonedSlide.isDefined) clonedSlide.get.convertSlideModel()
      }

      case SlideActionType.CopyFile => slideService.copyFileFromTheme(
        slideRequest.id.get,
        slideRequest.themeId.get
      )
    }
  })
}
