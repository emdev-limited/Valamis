package com.arcusys.valamis.web.servlet.export

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.export.service.ExportService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}

/**
 * Export statements to json and CSV
 */
class ExportServlet extends BaseApiController {

  private lazy val csvService = inject[ExportService]
  private lazy val fileService = inject[FileService]

  before() {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.TinCanStatementViewer)
  }

  get("/export/csv/statements(/)")(jsonAction {
    val req = CSVRequest(this)
    val filename = req.name

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("text/csv")

    val data = fileService.getFileContent("csv", filename)
    response.getOutputStream.write(data)
    response.getOutputStream.flush()
    response.getOutputStream.close()
  })

  get("/export/json/statements(/)")(jsonAction {
    val req = CSVRequest(this)
    val filename = req.name

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("application/json")

    val data = fileService.getFileContent("json", filename)
    response.getOutputStream.write(data)
    response.getOutputStream.flush()
    response.getOutputStream.close()
  })

  get("/export/status(/)")(jsonAction {
    val req = CSVRequest(this)
    val guid = req.guid

    csvService.getStatus(guid)
  })

  post("/export/export(/)") {
    val req = CSVRequest(this)
    val count = req.countOption
    val actor = req.actor
    val activity = req.activity
    val verb = req.verb
    val format = req.format
    val since = req.since
    val until = req.until
    val registration = req.registration
    val relatedAgents = req.relatedAgents
    val relatedActivities = req.relatedActivities

    csvService.export(count, actor, activity, verb, format, since, until, registration, relatedAgents, relatedActivities)
  }

  post("/export/cancel(/)") {
    val req = CSVRequest(this)
    val guid = req.guid

    csvService.cancel(guid)
    "ok"
  }

}
