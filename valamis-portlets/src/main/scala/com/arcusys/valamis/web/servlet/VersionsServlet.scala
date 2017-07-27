package com.arcusys.valamis.web.servlet

import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.util.BuildInfo

/**
  * Created by alezhoev on 19.06.17.
  */
class VersionsServlet extends BaseJsonApiController {
  get("/versions(/)")(
    Map("valamis" -> BuildInfo.version)
  )
}
