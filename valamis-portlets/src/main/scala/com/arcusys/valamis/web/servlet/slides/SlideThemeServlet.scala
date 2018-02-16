package com.arcusys.valamis.web.servlet.slides

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.slide.model.SlideTheme
import com.arcusys.valamis.slide.service.SlideThemeService
import com.arcusys.valamis.web.portlet.base.{EditThemePermission, ViewPermission}
import PortletName.LessonStudio
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.slides.request.SlideRequest

class SlideThemeServlet extends BaseApiController {

  private lazy val slideThemeService = inject[SlideThemeService]
  private lazy val req = SlideRequest(this)

  get("/slide-themes(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, LessonStudio)

    val userId = if (req.isMyThemes) Some(req.userId) else None
    slideThemeService.getBy(userId, req.isDefault)
  })

  get("/slide-themes/:id(/)")(jsonAction {
    slideThemeService.getById(req.id)
  })

  post("/slide-themes(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(EditThemePermission, LessonStudio)
    slideThemeService.create (
      SlideTheme (
        title = req.title,
        bgColor = req.bgColor,
        font = req.font,
        questionFont = req.questionFont,
        answerFont = req.answerFont,
        answerBg = req.answerBg,
        userId = if (req.isMyThemes) Some(req.userId) else None,
        isDefault = req.isDefault
      )
    )
  })

  post("/slide-themes/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(EditThemePermission, LessonStudio)
    slideThemeService.update (
      SlideTheme (
        id = req.id,
        title = req.title,
        bgColor = req.bgColor,
        font = req.font,
        questionFont = req.questionFont,
        answerFont = req.answerFont,
        answerBg = req.answerBg,
        userId = if (req.isMyThemes) Some(req.userId) else None,
        isDefault = req.isDefault
      )
    )
  })

  delete("/slide-themes/:id(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(EditThemePermission, LessonStudio)
    slideThemeService.delete(req.id)
  })
}
