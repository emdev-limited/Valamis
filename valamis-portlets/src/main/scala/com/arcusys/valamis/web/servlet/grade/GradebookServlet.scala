package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.gradebook.service.GradeBookService
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, ScalatraPermissionUtil}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}

class GradebookServlet extends BaseJsonApiController {

  lazy val gradebookService = inject[GradeBookService]
  override val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

  def permissionUtil = new ScalatraPermissionUtil(this)

  get("/gradebooks/user/:userId/lesson/:lessonId/statements(/)") {
    permissionUtil.requirePermissionApi(ViewPermission, PortletName.Gradebook)

    def userId = params.as[Long]("userId")
    def lessonId = params.as[Long]("lessonId")
    def page = params.getAs[Int]("page")
    def pageSize = params.as[Int]("count")
    def skipTake = page.map(p => new SkipTake((p - 1) * pageSize, pageSize))

    gradebookService.getAttemptsAndStatements(userId, lessonId, getCompanyId, skipTake)
  }
}
