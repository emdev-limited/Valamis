package com.arcusys.valamis.web.service

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.util.ParamUtilHelper

class OpenCertificateAction extends LBaseStrutsAction {

  override def execute(originalStrutsAction: LStrutsAction, request: HttpServletRequest, response: HttpServletResponse): String = {
    val resourcePrimKey: Long = ParamUtilHelper.getLong(request, "resourcePrimKey")
    val objectId = ParamUtilHelper.getLong(request, "oid")
    val plid = ParamUtilHelper.getLong(request, "plid")

    val lpId = if (objectId > 0) objectId else resourcePrimKey
    response.sendRedirect(s"/c/portal/learning-path/open?plid=$plid&lpId=$lpId")
    ""
  }
}
