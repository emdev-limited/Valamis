package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.facades.TranscriptPrintFacadeContract
import com.arcusys.learn.models.request.{PrintActionType, PrintRequest}

class TranscriptPrintApiController extends BaseApiController {

  lazy val printFacade = inject[TranscriptPrintFacadeContract]

  before() {
    response.setHeader("Pragma", "no-cache")
    response.setHeader("Cache-control", "must-revalidate,no-cache,no-store")
    response.setHeader("Expires", "-1")
    response.setHeader("Content-Disposition", "attachment; filename=\"transcript.pdf\"")
    response.setContentType("application/pdf")
    response.setCharacterEncoding("UTF-8")
  }

  get("/print(/)")(action {
    val printRequest = PrintRequest(this)

      printRequest.actionType match {
        case PrintActionType.PrintTranscript =>
          val companyId = printRequest.companyId
          val userId = printRequest.userId

          val templatesPath = servletContext.getRealPath("WEB-INF/fop")

          val out = printFacade.printTranscript(companyId, userId, templatesPath)

          response.setContentLength(out.size())
          response.getOutputStream.write(out.toByteArray)
          response.getOutputStream.flush()
          response.getOutputStream.close()
          out.close()
        case _ => new UnsupportedOperationException(s"Action ${printRequest.actionType} is not supported")
      }
  })
}
