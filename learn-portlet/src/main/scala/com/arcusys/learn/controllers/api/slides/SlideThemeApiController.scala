package com.arcusys.learn.controllers.api.slides

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.exceptions.BadRequestException
import com.arcusys.learn.liferay.permission.PortletName.LessonStudio
import com.arcusys.learn.liferay.permission.{EditThemePermission, PermissionUtil, ViewPermission}
import com.arcusys.learn.models.request.{SlideActionType, SlideRequest}
import com.arcusys.valamis.slide.model.SlideThemeModel
import com.arcusys.valamis.slide.service.SlideThemeServiceContract

class SlideThemeApiController extends BaseApiController {

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  private lazy val slideService = inject[SlideThemeServiceContract]

  private lazy val slideRequest = SlideRequest(this)

  get("/slidethemes(/)(:id)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    if(slideRequest.id.isEmpty){
      val userId = if (slideRequest.isMyThemes) Some(slideRequest.userId) else None
      slideService.getBy(userId, slideRequest.isDefault)
    }
    else
      slideService.getById(slideRequest.id.get)
  })

  post("/slidethemes(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(EditThemePermission, LessonStudio)

    slideRequest.action match {
      case SlideActionType.Create =>
        slideService.create (
          SlideThemeModel (
            None,
            slideRequest.title,
            slideRequest.bgColor,
            slideRequest.bgImage,
            slideRequest.font,
            slideRequest.questionFont,
            slideRequest.answerFont,
            slideRequest.answerBg,
            if (slideRequest.isMyThemes) Some(slideRequest.userId) else None,
            slideRequest.isDefault
          )
        )
      case SlideActionType.Update =>
        slideService.update (
          SlideThemeModel (
            slideRequest.id,
            slideRequest.title,
            slideRequest.bgColor,
            slideRequest.bgImage,
            slideRequest.font,
            slideRequest.questionFont,
            slideRequest.answerFont,
            slideRequest.answerBg,
            if (slideRequest.isMyThemes) Some(slideRequest.userId) else None,
            slideRequest.isDefault
          )
        )
      case SlideActionType.Delete =>
        slideService.delete(slideRequest.id.get)

      case _ => throw new BadRequestException
    }
  })
}
