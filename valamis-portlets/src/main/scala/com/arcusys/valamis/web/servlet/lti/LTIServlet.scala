package com.arcusys.valamis.web.servlet.lti

import com.arcusys.valamis.slide.service.lti.LTIDataService
import com.arcusys.valamis.slide.service.lti.model.LTIData
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.util.ForkJoinPoolWithLRCompany.ExecutionContext

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LTIServlet
  extends BaseJsonApiController {
  private lazy val ltiService = inject[LTIDataService]

  get("/lti/:uuid") {
    val uuid = params("uuid")


    Await.result(
      ltiService.get(uuid).map(_.getOrElse(new LTIData(uuid, Some("waiting"))))
      ,
      Duration.Inf
    )
  }

  delete("/lti/:uuid(/)") {
    val uuid = params("uuid")

    Await.result(ltiService.delete(uuid), Duration.Inf)
  }
}
