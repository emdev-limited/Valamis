package com.arcusys.valamis.web.servlet.slides

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.slide.model.SlideThemeModel
import com.arcusys.valamis.slide.service.SlideThemeServiceContract
import com.arcusys.valamis.web.portlet.base.{EditThemePermission, ViewPermission}
import PortletName.LessonStudio
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.slides.request.{SlideActionType, SlideRequest}

class SlideThemeServlet extends BaseApiController {

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
